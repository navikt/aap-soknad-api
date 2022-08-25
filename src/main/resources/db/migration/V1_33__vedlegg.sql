create table innsendtevedlegg
(
    id          serial primary key,
    soknad_id   integer     not null references soknader,
    eventid     varchar(50) not null
        constraint fk_eventid_soknad_vedlegg
            references soknader (eventid),
    created     timestamp   not null,
    updated     timestamp   not null,
    vedleggtype varchar(20) not null
);