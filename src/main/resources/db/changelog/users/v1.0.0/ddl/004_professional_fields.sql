SET search_path TO users;

ALTER TABLE vendor_profiles 
ADD COLUMN IF NOT EXISTS specialization TEXT[],
ADD COLUMN IF NOT EXISTS years_of_experience INTEGER,
ADD COLUMN IF NOT EXISTS service_area TEXT[],
ADD COLUMN IF NOT EXISTS bio TEXT,
ADD COLUMN IF NOT EXISTS certifications TEXT[],
ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20);

-- Also rename business_license to license_number if we want to align with checklist, 
-- but let's keep it and add an alias or just add the new one if they are different.
-- The checklist says licenseNumber is required. 001_tables.sql has business_license.
-- Let's just add license_number if we want to be exact, but the entity already has businessLicense.
-- I'll stick to the checklist names for the new columns.
