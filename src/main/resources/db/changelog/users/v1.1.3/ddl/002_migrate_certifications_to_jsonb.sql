SET search_path TO users;

-- Step 1: Add temporary jsonb column
ALTER TABLE vendor_profiles ADD COLUMN IF NOT EXISTS certifications_jsonb jsonb;

-- Step 2: Populate the temporary column with transformed data
UPDATE vendor_profiles
SET certifications_jsonb = CASE
    WHEN certifications IS NULL THEN '[]'::jsonb
    ELSE (
        SELECT COALESCE(jsonb_agg(jsonb_build_object('title', cert, 'fileKey', '')), '[]'::jsonb)
        FROM unnest(certifications) AS cert
    )
END;

-- Step 3: Drop the old column
ALTER TABLE vendor_profiles DROP COLUMN IF EXISTS certifications;

-- Step 4: Rename the temporary column to the original name
ALTER TABLE vendor_profiles RENAME COLUMN certifications_jsonb TO certifications;
