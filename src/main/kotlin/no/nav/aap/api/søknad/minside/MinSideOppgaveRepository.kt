package no.nav.aap.api.søknad.minside

import java.util.*
import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.minside.MinSideOppgaveRepository.Oppgave
import no.nav.aap.api.søknad.minside.MinSideRepository.EksternNotifikasjonBaseEntity
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity

interface MinSideOppgaveRepository : MinSideRepository<Oppgave> {

    @Entity(name = "oppgave")
    @Table(name = "minsideoppgaver")
    class Oppgave(fnr: String,
                  eventid: UUID,
                  @ManyToOne
                  var soknad: Søknad?,
                  @OneToMany(mappedBy = "oppgave", cascade = [ALL], orphanRemoval = true)
                  var notifikasjoner: MutableSet<EksternOppgaveNotifikasjon> = mutableSetOf()) :
        MinSideBaseEntity(fnr, eventid)

    @Entity(name = "eksternoppgavenotifikasjon")
    @Table(name = "eksterneoppgavenotifikasjoner")
    class EksternOppgaveNotifikasjon(@ManyToOne(optional = false)
                                     var oppgave: Oppgave? = null,
                                     eventid: UUID,
                                     distribusjonid: Long,
                                     distribusjonkanal: String) :
        EksternNotifikasjonBaseEntity(eventid, distribusjonid, distribusjonkanal)
}