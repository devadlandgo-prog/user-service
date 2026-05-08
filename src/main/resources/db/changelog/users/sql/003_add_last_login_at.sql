SET search_path TO users;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP NULL;
