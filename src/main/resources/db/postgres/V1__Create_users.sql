create table users
(
    id   serial
        constraint table_name_pk
            primary key,
    name varchar(255) not null
)