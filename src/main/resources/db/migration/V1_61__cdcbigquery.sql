CREATE PUBLICATION SOEKNAD_PUBLICATION FOR ALL TABLES;
SELECT PG_CREATE_LOGICAL_REPLICATION_SLOT ('AAP-SLOT', 'pgoutput');
CREATE USER REPLICATION_AAP_USER WITH REPLICATION IN ROLE
cloudsqlsuperuser LOGIN PASSWORD 'replication';

GRANT SELECT ON ALL TABLES IN SCHEMA public TO REPLICATION_AAP_USER;
GRANT USAGE ON SCHEMA public  TO REPLICATION_AAP_USER;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT ON TABLES TO REPLICATION_AAP_USER;