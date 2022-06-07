ALTER TABLE dittnavoppgaver
    ADD COLUMN updated TIMESTAMP;
ALTER TABLE dittnavoppgaver
    ALTER COLUMN done TYPE BOOLEAN;