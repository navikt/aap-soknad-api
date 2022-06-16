ALTER TABLE dittnavbeskjeder
    rename column ref to eventId;
ALTER TABLE dittnavoppgaver
    rename column ref to eventId;
ALTER TABLE soknader
    rename column ref to eventId;