INSERT INTO users (username, password, enabled, role)
SELECT 'admin', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', true, 'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');
