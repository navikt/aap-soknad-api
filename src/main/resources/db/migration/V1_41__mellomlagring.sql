ALTER TABLE minsidebeskjeder ADD COLUMN mellomlagring BOOLEAN;
UPDATE  minsidebeskjeder SET mellomlagring=FALSE;
ALTER TABLE minsidebeskjeder ALTER COLUMN mellomlagring SET NOT NULL;