-- Soft-delete unique constraints
ALTER TABLE account ADD CONSTRAINT unique_account_username UNIQUE NULLS NOT DISTINCT (username, deleted_at);
ALTER TABLE accommodation ADD CONSTRAINT unique_accommodation_name UNIQUE NULLS NOT DISTINCT (name, deleted_at);
ALTER TABLE accommodation ADD CONSTRAINT unique_accommodation_ses_code UNIQUE NULLS NOT DISTINCT (ses_code, deleted_at);