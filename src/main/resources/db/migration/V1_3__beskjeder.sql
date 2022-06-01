CREATE TABLE dittnavbeskjeder
(
    id      serial primary key,
    fnr     varchar(50) not null,
    ref     varchar(50) not null,
    created timestamp   not null
)