ALTER TABLE dittnavoppgaver
    ADD COLUMN updated TIMESTAMP;
ALTER TABLE dittnavoppgaver
    ADD COLUMN isDone BOOLEAN;
ALTER TABLE dittnavoppgaver
    DROP COLUMN done;