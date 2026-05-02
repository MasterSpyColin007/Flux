MERGE INTO users (id, username, password, enabled, role) KEY (username)
VALUES (1, 'admin', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', true, 'ROLE_ADMIN');

MERGE INTO users (id, username, password, enabled, role) KEY (username)
VALUES (2, 'alice', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', true, 'ROLE_USER');

MERGE INTO users (id, username, password, enabled, role) KEY (username)
VALUES (3, 'bob', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', true, 'ROLE_USER');

MERGE INTO users (id, username, password, enabled, role) KEY (username)
VALUES (4, 'charlie', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', true, 'ROLE_ADMIN');

MERGE INTO users (id, username, password, enabled, role) KEY (username)
VALUES (5, 'diana', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', false, 'ROLE_USER');

MERGE INTO users (id, username, password, enabled, role) KEY (username)
VALUES (6, 'eve', '$2a$10$2t324d2AfWGfYTVIxeUFM.aQWPuUBtsJbujEqjFE147J6SPOo9ytS', true, 'ROLE_USER');
