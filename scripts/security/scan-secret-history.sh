#!/bin/sh
set -eu
# RN-SEC106-001: complete history of develop and the delivery head only.
# Exit 0 = clean, 42 = findings, 2 = incomplete coverage/execution error.
fail() { printf '%s\n' "$1" >&2; exit 2; }
test "$#" -eq 5 || fail 'Usage: scan-secret-history.sh repository develop-ref delivery-ref config ignore-file'
repo=$1; develop=$2; delivery=$3; config=$4; ignore=$5
test -f "$config" && test -f "$ignore" || fail 'Scanner configuration or exception inventory missing'
shallow=$(git -C "$repo" rev-parse --is-shallow-repository) || fail 'Cannot inspect repository'
test "$shallow" = false || fail 'Incomplete history: shallow repository is forbidden'
base_sha=$(git -C "$repo" rev-parse --verify "$develop^{commit}") || fail 'Develop ref unavailable'
head_sha=$(git -C "$repo" rev-parse --verify "$delivery^{commit}") || fail 'Delivery ref unavailable'
printf 'History coverage: develop=%s delivery=%s; complete ancestry of both refs\n' "$base_sha" "$head_sha"
report=$(mktemp)
trap 'rm -f "$report"' EXIT
set +e
# RN-SEC106-002: inline allow comments cannot bypass the gate.
gitleaks git "$repo" --config "$config" --gitleaks-ignore-path "$ignore" \
  --log-opts="$base_sha $head_sha" --ignore-gitleaks-allow --exit-code 42 \
  --redact=100 --no-banner --log-level error --report-format json --report-path "$report"
status=$?
set -e
case "$status" in
  0) printf 'PASS: selected history contains no unexcepted findings\n';;
  42)
    count=$(grep -c '"Fingerprint":' "$report" || true)
    printf 'BLOCKED: %s historical findings; no credential validity inferred\n' "$count"
    exit 42;;
  *) fail 'ERROR: scanner failed; history gate is not PASS';;
esac
