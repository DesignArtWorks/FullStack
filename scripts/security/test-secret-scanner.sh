#!/bin/sh
set -eu
# Runs inside the pinned Gitleaks image. All values are synthetic, assembled at
# runtime so the negative controls themselves do not become history findings.
work=$(mktemp -d)
trap 'rm -r "$work"' EXIT
config=/repo/.gitleaks.toml
# Only commit-bound fingerprints may exempt reviewed historical occurrences.
# Reject broad exclusions even if the synthetic controls below still pass.
if grep -Eq '^[[:space:]]*(commits|paths|stopwords|fingerprints)[[:space:]]*=' "$config"; then
  printf 'Broad or unsupported secret-scanner exclusions are forbidden\n'
  exit 1
fi
if tr -d '\r' < /repo/.gitleaksignore | grep -Ev '^[[:space:]]*(#.*)?$' |
  grep -Ev '^[a-f0-9]{40}:[^:]+:[^:]+:[1-9][0-9]*$'; then
  printf 'Historical exceptions must be commit:path:rule:line fingerprints\n'
  exit 1
fi
blocked() {
  name=$1
  value=$2
  set +e
  printf '%s\n' "$value" | gitleaks stdin --config "$config" --redact=100 --no-banner --log-level error
  result=$?
  set -e
  if [ "$result" -ne 1 ]; then
    printf 'Expected detection for %s, got exit %s\n' "$name" "$result"
    exit 1
  fi
  printf 'Detected synthetic %s\n' "$name"
}
suffix=$(LC_ALL=C tr -dc 'A-Za-z0-9' < /dev/urandom | head -c 36)
blocked github "token=ghp_$suffix"
slack_prefix="xox"; slack_prefix="${slack_prefix}b"
blocked slack "token=${slack_prefix}-123456789012-123456789012-$(printf '%s' "$suffix" | cut -c 1-24)"
blocked aws "access_key=AKIA$(LC_ALL=C tr -dc 'A-Z0-9' < /dev/urandom | head -c 16)"
blocked pem "$(printf '%s\n' '-----BEGIN RSA PRIVATE'' KEY-----' 'c3ludGhldGljLW5vdC1hLXJlYWwta2V5' '-----END RSA PRIVATE'' KEY-----')"
printf '%s\n' 'NEXTAUTH_SECRET="ci-only-nextauth-secret-not-for-production"' |
  gitleaks stdin --config "$config" --redact=100 --no-banner --log-level error

# A removed secret must still be found by a complete-history scan.
git -C "$work" init -q
git -C "$work" config user.email fixture@example.test
git -C "$work" config user.name 'Synthetic fixture'
printf 'token=ghp_%s\n' "$suffix" > "$work/fixture.txt"
git -C "$work" add fixture.txt
git -C "$work" commit -qm 'Synthetic negative fixture'
git -C "$work" rm -q fixture.txt
git -C "$work" commit -qm 'Remove fixture'
set +e
gitleaks git "$work" --log-opts=--all --config "$config" --redact=100 --no-banner --log-level error
result=$?
set -e
test "$result" -eq 1
printf 'Detected removed historical fixture; placeholder accepted\n'

# Exempting one occurrence must not exempt its siblings or future copies.
printf 'token=ghp_%s\ntoken=ghp_%s\n' "$suffix" "$suffix" > "$work/pair.txt"
git -C "$work" add pair.txt
git -C "$work" commit -qm 'Two synthetic findings in one commit'
pair_commit=$(git -C "$work" rev-parse HEAD)
git -C "$work" rm -q pair.txt
git -C "$work" commit -qm 'Remove paired findings'
first_commit=$(git -C "$work" rev-list --max-parents=0 HEAD)
printf '%s:fixture.txt:github-pat:1\n%s:pair.txt:github-pat:1\n' \
  "$first_commit" "$pair_commit" > "$work/.gitleaksignore"

scan_fixture() {
  expected_exit=$1
  expected_count=$2
  set +e
  gitleaks git "$work" --log-opts=--all --config "$config" \
    --gitleaks-ignore-path "$work/.gitleaksignore" --redact=100 \
    --no-banner --log-level error --report-format json --report-path "$work/result.json"
  result=$?
  set -e
  test "$result" -eq "$expected_exit"
  count=$(grep -c '"Fingerprint":' "$work/result.json" || true)
  test "$count" -eq "$expected_count"
}
scan_fixture 1 1
grep -Fq "\"Fingerprint\": \"${pair_commit}:pair.txt:github-pat:2\"" "$work/result.json"
printf 'Same-commit sibling remains detected\n'

printf '%s:pair.txt:github-pat:2\n' "$pair_commit" >> "$work/.gitleaksignore"
scan_fixture 0 0
printf 'Exact reviewed historical occurrences accepted\n'

printf 'token=ghp_%s\n' "$suffix" > "$work/pair.txt"
printf 'token=ghp_%s\n' "$suffix" > "$work/new-file.txt"
git -C "$work" add pair.txt new-file.txt
git -C "$work" commit -qm 'Reintroduce synthetic values in a new commit'
new_commit=$(git -C "$work" rev-parse HEAD)
scan_fixture 1 2
grep -Fq "\"Fingerprint\": \"${new_commit}:pair.txt:github-pat:1\"" "$work/result.json"
grep -Fq "\"Fingerprint\": \"${new_commit}:new-file.txt:github-pat:1\"" "$work/result.json"
printf 'New commit and new file remain detected\n'
