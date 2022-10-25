ALTER TABLE minsidebeskjeder
   ALTER COLUMN created TYPE timestamptz;
ALTER TABLE minsidebeskjeder
    ALTER COLUMN updated TYPE timestamptz;

ALTER TABLE minsideoppgaver
    ALTER COLUMN created TYPE timestamptz;
ALTER TABLE minsideoppgaver
    ALTER COLUMN updated TYPE timestamptz;

ALTER TABLE manglendevedlegg
    ALTER COLUMN created TYPE timestamptz;
ALTER TABLE manglendevedlegg
    ALTER COLUMN updated TYPE timestamptz;

ALTER TABLE innsendtevedlegg
    ALTER COLUMN created TYPE timestamptz;
ALTER TABLE innsendtevedlegg
   ALTER COLUMN updated TYPE timestamptz;


ALTER TABLE soknader
ALTER COLUMN created TYPE timestamptz;
ALTER TABLE soknader
ALTER COLUMN updated TYPE timestamptz;