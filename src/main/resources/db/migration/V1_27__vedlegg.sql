ALTER TABLE soknader
    ADD UNIQUE (eventid);

create table manglendevedlegg
(
    id          serial primary key,
    soknad_id   int         not null references soknader (id),
    eventid     varchar(50) not null,
    created     timestamp   not null,
    updated     timestamp   not null,
    vedleggtype varchar(10) not null,
    CONSTRAINT fk_eventid_soknad
        FOREIGN KEY (eventid)
            REFERENCES soknader (eventid)
)