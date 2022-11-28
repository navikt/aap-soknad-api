package no.nav.aap.api.søknad.minside

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.*
import no.nav.aap.api.søknad.minside.MinSideOppgaveRepository.Oppgave
import no.nav.aap.api.søknad.minside.MinSideRepository.EksternNotifikasjonBaseEntity
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity

interface MinSideOppgaveRepository : MinSideRepository<Oppgave> {

    @Entity(name = "oppgave")
    @Table(name = "minsideoppgaver")
    class Oppgave(fnr: String,
                  eventid: UUID,
                  done: Boolean = false,
                  ekstern: Boolean = false,
                  @OneToMany(mappedBy = "oppgave", cascade = [ALL], orphanRemoval = true)
                  var notifikasjoner: MutableSet<EksternOppgaveNotifikasjon> = mutableSetOf()) :
        MinSideBaseEntity(fnr, eventid, done,ekstern)

    @Entity(name = "eksternoppgavenotifikasjon")
    @Table(name = "eksterneoppgavenotifikasjoner")
    class EksternOppgaveNotifikasjon(@ManyToOne(optional = false)
                                     var oppgave: Oppgave? = null,
                                     eventid: UUID,
                                     distribusjonid: Long,
                                     distribusjonkanal: String) :
        EksternNotifikasjonBaseEntity(eventid, distribusjonid, distribusjonkanal)
}