CREATE TABLE ettersendinger
(
    id            serial primary key,
    fnr           VARCHAR(255),
    eventid       VARCHAR(50),
    updated       TIMESTAMP not null,
    created       TIMESTAMP not null,
    journalpostid VARCHAR(255),
    soknad_id     integer   not null references soknader (id)
);