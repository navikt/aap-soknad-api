ALTER TABLE dittnavbeskjeder
    ADD COLUMN ekstern BOOLEAN;
ALTER TABLE dittnavoppgaver
    ADD COLUMN ekstern BOOLEAN;
UPDATE dittnavbeskjeder SET ekstern = false;
UPDATE dittnavoppgaver SET ekstern = false;
ALTER TABLE dittnavbeskjeder ALTER COLUMN ekstern SET NOT NULL;
ALTER TABLE dittnavoppgaver ALTER COLUMN ekstern SET NOT NULL;