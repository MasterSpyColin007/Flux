MERGE INTO users (id, username, password, enabled, dark_mode, role) KEY (username)
VALUES (1, 'admin', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', true, false, 'ROLE_ADMIN');
