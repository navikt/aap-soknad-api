alter table dittnavoppgaver
    DROP column distribusjondato,
    DROP column distribusjonkanal,
    DROP column distribusjonid;

alter table dittnavbeskjeder
    DROP column distribusjondato,
    DROP column distribusjonkanal,
    DROP column distribusjonid;

alter table eksternenotifikasjoner
    RENAME TO eksterneoppgavenotifikasjoner;

create table eksternebeskjednotifikasjoner
(
    id                serial primary key,
    beskjed_id        int         not null references dittnavbeskjeder (id),
    eventid           varchar(50) not null,
    distribusjonid    varchar(50) not null,
    distribusjonkanal varchar(50) not null,
    distribusjondato  timestamp   not null,
    CONSTRAINT fk_eventid_neskjed
        FOREIGN KEY (eventid)
            REFERENCES dittnavbeskjeder (eventid)
)