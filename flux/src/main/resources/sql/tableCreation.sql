create table users (
id integer auto_increment primary key,
username varchar(255) unique,
email varchar(255) unique,
password varchar(255),
bio varchar(500),
enabled boolean,
dark_mode boolean default false,
role varchar(31));


create table posts (
id integer auto_increment primary key,
user_id int,
title varchar(255),
content text,
image_url varchar(1024),
created_at timestamp,
CONSTRAINT fk_user
    FOREIGN KEY (user_id)
    REFERENCES users(id));

create table post_comments (
id integer auto_increment primary key,
post_id int,
user_id int,
parent_id int,
content varchar(1000),
created_at timestamp,
CONSTRAINT fk_comment_post
    FOREIGN KEY (post_id)
    REFERENCES posts(id),
CONSTRAINT fk_comment_user
    FOREIGN KEY (user_id)
    REFERENCES users(id),
CONSTRAINT fk_comment_parent
    FOREIGN KEY (parent_id)
    REFERENCES post_comments(id));
