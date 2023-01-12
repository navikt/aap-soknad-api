package no.nav.aap.api.søknad.minside

import java.util.*
import javax.persistence.CascadeType.ALL
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table
import no.nav.aap.api.søknad.minside.MinSideOppgaveRepository.Oppgave
import no.nav.aap.api.søknad.minside.MinSideRepository.EksternNotifikasjonBaseEntity
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity

interface MinSideOppgaveRepository : MinSideRepository<Oppgave> {

    @Entity(name = "oppgave")
    @Table(name = "minsideoppgaver")
    class Oppgave(fnr: String,
                  eventid: UUID,
               //   done: Boolean = false,
                  ekstern: Boolean = false,
                  /*
                  @OneToOne(mappedBy = "id")
                  var søknad: Søknad?,*/
                  @OneToMany(mappedBy = "oppgave", cascade = [ALL], orphanRemoval = true)
                  var notifikasjoner: MutableSet<EksternOppgaveNotifikasjon> = mutableSetOf()) :
        MinSideBaseEntity(fnr, eventid,/* done,*/ekstern)

    @Entity(name = "eksternoppgavenotifikasjon")
    @Table(name = "eksterneoppgavenotifikasjoner")
    class EksternOppgaveNotifikasjon(@ManyToOne(optional = false)
                                     var oppgave: Oppgave? = null,
                                     eventid: UUID,
                                     distribusjonid: Long,
                                     distribusjonkanal: String) :
        EksternNotifikasjonBaseEntity(eventid, distribusjonid, distribusjonkanal)
}