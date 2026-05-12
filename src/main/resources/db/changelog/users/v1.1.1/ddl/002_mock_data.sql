SET search_path TO users;

-- Clean existing mock data if needed (optional, depends on your preference)
-- DELETE FROM vendor_profiles WHERE id IN ('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440005');
-- DELETE FROM users WHERE id IN ('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440005');

-- 550e8400-e29b-41d4-a716-446655440001: Michael Chen (Elite Land Developments)
INSERT INTO users (id, full_name, email, role, is_professional, active) 
VALUES ('550e8400-e29b-41d4-a716-446655440001', 'Michael Chen', 'm.chen@eliteland.ca', 'VENDOR', true, true)
ON CONFLICT (id) DO NOTHING;
INSERT INTO vendor_profiles (id, user_id, company_name, business_address, business_city, business_state, business_zip_code, business_country, verified, verification_status, rating, total_reviews)
VALUES ('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'Elite Land Developments', '120 Adelaide St W', 'Toronto', 'ON', 'M5H 1T1', 'Canada', true, 'APPROVED', 4.9, 12)
ON CONFLICT (id) DO NOTHING;

-- 550e8400-e29b-41d4-a716-446655440002: Sarah Thompson (Thompson & Co.)
INSERT INTO users (id, full_name, email, role, is_professional, active) 
VALUES ('550e8400-e29b-41d4-a716-446655440002', 'Sarah Thompson', 'sarah@thompsonco.ca', 'VENDOR', true, true)
ON CONFLICT (id) DO NOTHING;
INSERT INTO vendor_profiles (id, user_id, company_name, business_address, business_city, business_state, business_zip_code, business_country, verified, verification_status, rating, total_reviews)
VALUES ('550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', 'Thompson & Co. Real Estate', '45 Main St S', 'Mississauga', 'ON', 'L5M 1J3', 'Canada', true, 'APPROVED', 4.7, 8)
ON CONFLICT (id) DO NOTHING;

-- 550e8400-e29b-41d4-a716-446655440003: David Rodriguez (Urban Sprout)
INSERT INTO users (id, full_name, email, role, is_professional, active) 
VALUES ('550e8400-e29b-41d4-a716-446655440003', 'David Rodriguez', 'd.rodriguez@urbansprout.ca', 'VENDOR', true, true)
ON CONFLICT (id) DO NOTHING;
INSERT INTO vendor_profiles (id, user_id, company_name, business_address, business_city, business_state, business_zip_code, business_country, verified, verification_status, rating, total_reviews)
VALUES ('550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440003', 'Urban Sprout Land Group', '201 King St E', 'Hamilton', 'ON', 'L8N 1B6', 'Canada', false, 'PENDING', 4.5, 3)
ON CONFLICT (id) DO NOTHING;

-- 550e8400-e29b-41d4-a716-446655440004: Jennifer Wu (Pacific Rim)
INSERT INTO users (id, full_name, email, role, is_professional, active) 
VALUES ('550e8400-e29b-41d4-a716-446655440004', 'Jennifer Wu', 'jwu@pacificrimdev.ca', 'VENDOR', true, true)
ON CONFLICT (id) DO NOTHING;
INSERT INTO vendor_profiles (id, user_id, company_name, business_address, business_city, business_state, business_zip_code, business_country, verified, verification_status, rating, total_reviews)
VALUES ('550e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440004', 'Pacific Rim Developers', '800 West Pender St', 'Vancouver', 'BC', 'V6C 2V6', 'Canada', true, 'APPROVED', 4.8, 15)
ON CONFLICT (id) DO NOTHING;

-- 550e8400-e29b-41d4-a716-446655440005: Robert Miller (Keystone)
INSERT INTO users (id, full_name, email, role, is_professional, active) 
VALUES ('550e8400-e29b-41d4-a716-446655440005', 'Robert Miller', 'r.miller@keystonepartners.ca', 'VENDOR', true, true)
ON CONFLICT (id) DO NOTHING;
INSERT INTO vendor_profiles (id, user_id, company_name, business_address, business_city, business_state, business_zip_code, business_country, verified, verification_status, rating, total_reviews)
VALUES ('550e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440005', 'Keystone Property Partners', '150 Laurier Ave W', 'Ottawa', 'ON', 'K1P 5J4', 'Canada', false, 'REJECTED', 3.2, 2)
ON CONFLICT (id) DO NOTHING;
