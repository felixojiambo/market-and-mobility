ALTER TABLE users
    ADD COLUMN first_name VARCHAR(80),
    ADD COLUMN last_name  VARCHAR(80);

UPDATE users
SET first_name = COALESCE(first_name, 'Unknown'),
    last_name  = COALESCE(last_name, 'User');

ALTER TABLE users
    ALTER COLUMN first_name SET NOT NULL,
ALTER COLUMN last_name SET NOT NULL;
