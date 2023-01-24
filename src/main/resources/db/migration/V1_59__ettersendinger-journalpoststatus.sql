ALTER TABLE ettersendinger ADD COLUMN journalpoststatus varchar(35);
CREATE INDEX JOURNALPOST_ETTERSENDING_IX ON ettersendinger (JOURNALPOSTID);