SET search_path TO users;

CREATE TABLE IF NOT EXISTS expertise_options (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed initial expertise options
INSERT INTO expertise_options (id, name, description) VALUES
(gen_random_uuid(), 'REALTOR', 'Professional real estate agent'),
(gen_random_uuid(), 'DEVELOPER', 'Land and property developer'),
(gen_random_uuid(), 'CONTRACTOR', 'General or specialized contractor'),
(gen_random_uuid(), 'BROKER', 'Real estate broker'),
(gen_random_uuid(), 'Land Surveying', 'Professional land surveyors'),
(gen_random_uuid(), 'Architecture', 'Architectural design services'),
(gen_random_uuid(), 'Legal Advice', 'Legal services for real estate'),
(gen_random_uuid(), 'Civil Engineering', 'Civil engineering services'),
(gen_random_uuid(), 'Environmental Assessment', 'Environmental impact and assessment services'),
(gen_random_uuid(), 'Urban Planning', 'City and urban planning services'),
(gen_random_uuid(), 'Real Estate Law', 'Specialized real estate legal services'),
(gen_random_uuid(), 'Property Appraisal', 'Professional property valuation services')
ON CONFLICT (name) DO NOTHING;
