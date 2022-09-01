classDiagram
direction BT
class eksternebeskjednotifikasjoner {
integer beskjed_id
varchar(50) eventid
varchar(50) distribusjonid
varchar(50) distribusjonkanal
timestamp created
integer id
}
class eksterneoppgavenotifikasjoner {
varchar(50) distribusjonid
varchar(50) distribusjonkanal
timestamp created
varchar(50) eventid
integer oppgave_id
integer id
}
class innsendtevedlegg {
integer soknad_id
varchar(50) eventid
timestamp created
timestamp updated
varchar(20) vedleggtype
integer id
}
class manglendevedlegg {
integer soknad_id
varchar(50) eventid
timestamp created
timestamp updated
varchar(20) vedleggtype
integer id
}
class minsidebeskjeder {
varchar(50) fnr
varchar(50) eventid
timestamp created
timestamp updated
boolean done
integer id
}
class minsideoppgaver {
varchar(50) fnr
varchar(50) eventid
timestamp created
timestamp updated
boolean done
integer id
}
class soknader {
varchar(50) fnr
varchar(50) eventid
timestamp created
timestamp updated
varchar(50) journalpostid
integer id
}

eksternebeskjednotifikasjoner -->  minsidebeskjeder : beskjed_id:id
eksternebeskjednotifikasjoner -->  minsidebeskjeder : eventid
eksterneoppgavenotifikasjoner -->  minsideoppgaver : oppgave_id:id
innsendtevedlegg -->  soknader : eventid
innsendtevedlegg -->  soknader : soknad_id:id
manglendevedlegg -->  soknader : soknad_id:id
manglendevedlegg -->  soknader : eventid