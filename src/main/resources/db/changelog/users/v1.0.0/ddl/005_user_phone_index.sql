SET search_path TO users;

-- Clean up duplicate phone numbers by keeping only the most recent one
-- This ensures the unique index creation below does not fail due to existing dirty data
UPDATE users 
SET phone = NULL 
WHERE id IN (
    SELECT id 
    FROM (
        SELECT id, ROW_NUMBER() OVER (PARTITION BY phone ORDER BY created_at DESC) as row_num 
        FROM users 
        WHERE phone IS NOT NULL
    ) t 
    WHERE t.row_num > 1
);

-- Add index and unique constraint to phone column to support login by phone
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_phone_unique ON users(phone) WHERE phone IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
