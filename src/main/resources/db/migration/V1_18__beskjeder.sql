ALTER TABLE dittnavbeskjeder
    ADD COLUMN distribusjondato  timestamp,
    ADD COLUMN distribusjonid    integer,
    ADD COLUMN distribusjonkanal varchar(10);
ALTER TABLE dittnavoppgaver
    ADD COLUMN distribusjondato  timestamp,
    ADD COLUMN distribusjonid    integer,
    ADD COLUMN distribusjonkanal varchar(10);