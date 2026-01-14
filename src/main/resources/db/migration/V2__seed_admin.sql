-- V2__seed_admin.sql
-- Seed an admin account for development/testing.
-- Email: admin@cymelle.com
-- Password: Admin@123
-- BCrypt hash below corresponds to Admin@123 (BCrypt)

INSERT INTO users (email, password_hash, role)
SELECT 'admin@cymelle.com',
       '$2a$10$7EqJtq98hPqEX7fNZaFWoO5rCj0Xwz6v8tZcV0QfF5z2m5LkYV7uS',
       'ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@cymelle.com'
);
