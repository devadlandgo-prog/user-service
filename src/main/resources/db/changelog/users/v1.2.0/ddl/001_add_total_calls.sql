SET search_path TO users;

ALTER TABLE vendor_profiles ADD COLUMN IF NOT EXISTS total_calls integer DEFAULT 0 NOT NULL;
