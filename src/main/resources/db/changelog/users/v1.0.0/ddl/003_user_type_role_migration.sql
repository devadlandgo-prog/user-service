SET search_path TO users;

-- Add isProfessional column
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_professional BOOLEAN NOT NULL DEFAULT FALSE;

-- Migrate existing data:
-- AGENT users become professionals
UPDATE users SET is_professional = true WHERE role = 'AGENT' OR user_type = 'AGENT';

-- Standardize roles: SELLER → VENDOR, AGENT → VENDOR
UPDATE users SET role = 'VENDOR' WHERE role IN ('SELLER', 'AGENT');

-- Standardize user_type: AGENT → SELLER
UPDATE users SET user_type = 'SELLER' WHERE user_type = 'AGENT';
