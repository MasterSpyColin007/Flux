create table users (
id integer auto_increment primary key,
username varchar(255) unique,
email varchar(255) unique,
password varchar(255),
bio varchar(500),
enabled boolean,
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
