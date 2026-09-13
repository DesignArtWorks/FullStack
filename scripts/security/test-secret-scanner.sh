#!/bin/sh
set -eu
# Runs inside the pinned Gitleaks image. All values are synthetic, assembled at
# runtime so the negative controls themselves do not become history findings.
work=$(mktemp -d)
trap 'rm -r "$work"' EXIT
config=/repo/.gitleaks.toml
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
blocked slack "token=xoxb-123456789012-123456789012-$(printf '%s' "$suffix" | cut -c 1-24)"
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
