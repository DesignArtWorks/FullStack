#!/bin/sh
set -eu
# Synthetic values only; no credentials are authenticated.
work=$(mktemp -d)
trap 'rm -r "$work"' EXIT
config=/repo/.gitleaks.toml
git init -q "$work/source"
git -C "$work/source" config user.email fixture@example.test
git -C "$work/source" config user.name Fixture
suffix=$(LC_ALL=C tr -dc 'A-Za-z0-9' < /dev/urandom | head -c 36)
printf 'token=ghp_%s\n' "$suffix" > "$work/source/fixture.txt"
git -C "$work/source" add .
git -C "$work/source" commit -qm 'Historical synthetic finding'
git -C "$work/source" rm -q fixture.txt
git -C "$work/source" commit -qm 'Remove synthetic finding'
git clone -q --depth=1 "file://$work/source" "$work/shallow"
set +e
gitleaks git "$work/shallow" --config "$config" --log-opts=--all --exit-code 42 --redact=100 --no-banner --log-level error
status=$?
set -e
if [ "${1:-}" = baseline ]; then
  if [ "$status" -ne 42 ]; then
    printf 'RED: depth=1 misses a removed historical secret (exit %s, expected 42)\n' "$status"
    exit 1
  fi
fi
test "$status" -eq 0
scan=/repo/scripts/security/scan-secret-history.sh
expect() {
  expected=$1; shift
  set +e
  sh "$scan" "$@"
  status=$?
  set -e
  test "$status" -eq "$expected" || { printf 'Expected %s, got %s\n' "$expected" "$status"; exit 1; }
}
touch "$work/ignore"
expect 2 "$work/shallow" HEAD HEAD "$config" "$work/ignore"
expect 2 "$work/source" missing-ref HEAD "$config" "$work/ignore"
expect 42 "$work/source" HEAD HEAD "$config" "$work/ignore"
git init -q "$work/branches"
git -C "$work/branches" config user.email fixture@example.test
git -C "$work/branches" config user.name Fixture
git -C "$work/branches" commit --allow-empty -qm Base
base=$(git -C "$work/branches" rev-parse HEAD)
git -C "$work/branches" branch develop
git -C "$work/branches" branch pilot
git -C "$work/branches" checkout -qb unrelated
printf 'token=ghp_%s\n' "$suffix" > "$work/branches/other.txt"
git -C "$work/branches" add .
git -C "$work/branches" commit -qm 'Unrelated synthetic finding'
expect 0 "$work/branches" develop pilot "$config" "$work/ignore"
for ref in develop pilot; do
  git -C "$work/branches" checkout -q "$ref"
  printf 'token=ghp_%s\n' "$suffix" > "$work/branches/$ref.txt"
  git -C "$work/branches" add .
  git -C "$work/branches" commit -qm 'Selected branch synthetic finding'
  git -C "$work/branches" rm -q "$ref.txt"
  git -C "$work/branches" commit -qm 'Remove finding'
  expect 42 "$work/branches" develop pilot "$config" "$work/ignore"
  # Independent proof: the other selected ref is clean, so it cannot mask failure.
  expect 42 "$work/branches" "$base" "$ref" "$config" "$work/ignore"
done
# Scanner execution error is different from a finding and never PASS.
mkdir "$work/bin"
printf '#!/bin/sh\nexit 1\n' > "$work/bin/gitleaks"
chmod +x "$work/bin/gitleaks"
PATH="$work/bin:$PATH" expect 2 "$work/source" HEAD HEAD "$config" "$work/ignore"
printf 'PASS: full history, both selected branches, unrelated exclusion, shallow/missing refs and scanner errors\n'
