CREATE TABLE søknader
(
    id        serial primary key,
    fnr       varchar(50) not null,
    ref       varchar(50) not null,
    jti       varchar(50) not null,
    created   timestamp   not null,
    updated   timestamp   not null,
    gyldigtil timestamp   not null
)