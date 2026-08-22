USE crop_portal;
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_DOCTOR'), ('ROLE_FARMER');
INSERT IGNORE INTO crops (name, season, description) VALUES
('Rice', 'Kharif', 'Rice crop category'),
('Wheat', 'Rabi', 'Wheat crop category'),
('Tomato', 'All season', 'Tomato crop category'),
('Cotton', 'Kharif', 'Cotton crop category');

-- Runtime seed users are created by Spring Boot with BCrypt passwords.
-- Demo credentials:
-- admin@crop.ai / Password@123
-- doctor@crop.ai / Password@123
-- farmer@crop.ai / Password@123
