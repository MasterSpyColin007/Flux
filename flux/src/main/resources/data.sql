INSERT INTO users (username, password, enabled, role)
SELECT 'admin', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', true, 'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password, enabled, role)
SELECT 'alice', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', true, 'ROLE_USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'alice');

INSERT INTO users (username, password, enabled, role)
SELECT 'bob', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', true, 'ROLE_USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'bob');

INSERT INTO users (username, password, enabled, role)
SELECT 'charlie', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', true, 'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'charlie');

INSERT INTO users (username, password, enabled, role)
SELECT 'diana', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', false, 'ROLE_USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'diana');

INSERT INTO users (username, password, enabled, role)
SELECT 'eve', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', true, 'ROLE_USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'eve');
