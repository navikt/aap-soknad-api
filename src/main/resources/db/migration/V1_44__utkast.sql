CREATE TABLE minsideutkast
(
    id serial primary key,
    type varchar(10) not null,
    eventid varchar(50) not null,
    fnr varchar(50) not null,
    done boolean,
    created timestamp not null,
    updated timestamp not null
);

CREATE INDEX FNR_UTKAST_IX ON minsideutkast(fnr);
CREATE INDEX EVENTID_UTKAST_IX ON minsideutkast(eventid);