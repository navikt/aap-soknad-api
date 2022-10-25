ALTER TABLE minsidebeskjeder
   ALTER COLUMN created TYPE timestamp;
ALTER TABLE minsidebeskjeder
    ALTER COLUMN updated TYPE timestamp;

ALTER TABLE minsideoppgaver
    ALTER COLUMN created TYPE timestamp;
ALTER TABLE minsideoppgaver
    ALTER COLUMN updated TYPE timestamp;

ALTER TABLE manglendevedlegg
    ALTER COLUMN created TYPE timestamp;
ALTER TABLE manglendevedlegg
    ALTER COLUMN updated TYPE timestamp;

ALTER TABLE innsendtevedlegg
    ALTER COLUMN created TYPE timestamp;
ALTER TABLE innsendtevedlegg
   ALTER COLUMN updated TYPE timestamp;


ALTER TABLE soknader
ALTER COLUMN created TYPE timestamp;
ALTER TABLE soknader
ALTER COLUMN updated TYPE timestamp;