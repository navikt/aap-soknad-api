alter table eksternebeskjednotifikasjoner
    rename column distribusjondato to created;
alter table eksterneoppgavenotifikasjoner
    rename column distribusjondato to created;