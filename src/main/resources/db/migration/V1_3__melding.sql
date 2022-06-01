CREATE TABLE dittnavbeskjeder
(
    id      SERIAL PRIMARY KEY,
    fnr     VARCHAR(50),
    ref     VARCHAR(50),
    created TIMESTAMP NOT NULL
);