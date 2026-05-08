SET search_path TO users;

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_provider ON users(auth_provider, provider_id);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

CREATE INDEX IF NOT EXISTS idx_email_token_user ON email_verification_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_password_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_token_user ON password_reset_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_notif_pref_user ON notification_preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_vendor_user ON vendor_profiles(user_id);
