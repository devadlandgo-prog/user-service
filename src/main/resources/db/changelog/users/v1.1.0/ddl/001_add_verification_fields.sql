SET search_path TO users;

ALTER TABLE vendor_profiles ADD COLUMN IF NOT EXISTS verification_status VARCHAR(20) DEFAULT 'PENDING';
ALTER TABLE vendor_profiles ADD COLUMN IF NOT EXISTS verification_notes VARCHAR(500);
