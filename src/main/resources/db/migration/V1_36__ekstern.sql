ALTER TABLE minsidebeskjeder
    ADD COLUMN ekstern BOOLEAN;
ALTER TABLE minsideoppgaver
    ADD COLUMN ekstern BOOLEAN;
UPDATE minsidebeskjeder set ekstern = FALSE;
UPDATE minsideoppgaver set ekstern = FALSE;
ALTER TABLE minsidebeskjeder ALTER COLUMN ekstern SET NOT NULL;
ALTER TABLE minsideoppgaver ALTER COLUMN ekstern SET NOT NULL;