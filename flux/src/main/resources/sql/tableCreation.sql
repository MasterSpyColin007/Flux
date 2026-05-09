create table users (
id integer auto_increment primary key,
username varchar(255) unique,
password varchar(255),
enabled boolean,
role varchar(31));


create table posts (
id integer auto_increment primary key,
user_id int,
title varchar(255),
content text,
CONSTRAINT fk_user
    FOREIGN KEY (user_id)
    REFERENCES users(id));
