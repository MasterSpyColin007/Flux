MERGE INTO users (id, username, password, enabled, role) KEY (username)
VALUES (1, 'admin', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', true, 'ROLE_ADMIN');
