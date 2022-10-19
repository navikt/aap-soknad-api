UPDATE  minsidebeskjeder SET ekstern=FALSE;
UPDATE  minsideoppgaver SET ekstern=FALSE;
ALTER TABLE minsidebeskjeder ALTER COLUMN ekstern SET NOT NULL;
ALTER TABLE minsideoppgaver ALTER COLUMN ekstern SET NOT NULL;