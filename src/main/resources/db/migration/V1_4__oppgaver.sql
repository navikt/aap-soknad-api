CREATE TABLE dittnavoppgaver
(
    id      serial primary key,
    fnr     varchar(50) not null,
    ref     varchar(50) not null,
    done    timestamp,
    created timestamp   not null
)