-- Legacy hashes were derived from recoverable plaintext. Erasing plaintext alone
-- would leave those bearer values active. Revoke only rows carrying that legacy
-- representation; preserve newly issued hash-only tokens and their lifecycle.
UPDATE team_invitations
SET active = false, expires_at = LEAST(expires_at, CURRENT_TIMESTAMP), token_preview = NULL
WHERE token IS NOT NULL;

UPDATE password_reset_tokens
SET used_at = COALESCE(used_at, CURRENT_TIMESTAMP),
    expires_at = LEAST(expires_at, CURRENT_TIMESTAMP), token_preview = NULL
WHERE token IS NOT NULL;

ALTER TABLE team_invitations DROP COLUMN IF EXISTS token;
ALTER TABLE password_reset_tokens DROP COLUMN IF EXISTS token;
