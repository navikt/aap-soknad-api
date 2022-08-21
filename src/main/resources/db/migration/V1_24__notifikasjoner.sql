alter table eksternenotifikasjoner
    add column oppgaveid int not null references dittnavoppgaver (id);