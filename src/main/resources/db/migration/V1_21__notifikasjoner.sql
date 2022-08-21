ALTER TABLE dittnavoppgaver
    ADD UNIQUE (eventid);
ALTER TABLE dittnavbeskjeder
    ADD UNIQUE (eventid);

CREATE TABLE eksternenotifikasjoner
(
    id                serial primary key,
    eventid           varchar(50) not null,
    distribusjonid    varchar(50) not null,
    distribusjonkanal varchar(50) not null,
    distribusjondato  timestamp   not null,
    CONSTRAINT fk_eventid
        FOREIGN KEY (eventid)
            REFERENCES dittnavoppgaver (eventid)
)