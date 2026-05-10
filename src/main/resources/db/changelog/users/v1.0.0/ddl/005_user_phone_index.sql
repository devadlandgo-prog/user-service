SET search_path TO users;

-- Add index and unique constraint to phone column to support login by phone
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_phone_unique ON users(phone) WHERE phone IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
