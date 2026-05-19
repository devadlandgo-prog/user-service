SET search_path TO users;

ALTER TABLE vendor_profiles
  ALTER COLUMN certifications TYPE jsonb
  USING CASE
    WHEN certifications IS NULL THEN '[]'::jsonb
    ELSE (
      SELECT jsonb_agg(jsonb_build_object('title', cert, 'fileKey', cert))
      FROM unnest(certifications) AS cert
    )
  END;
