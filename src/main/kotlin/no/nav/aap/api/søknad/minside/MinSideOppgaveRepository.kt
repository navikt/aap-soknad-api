package no.nav.aap.api.søknad.minside

import no.nav.aap.api.søknad.minside.MinSideOppgaveRepository.Oppgave
import no.nav.aap.api.søknad.minside.MinSideRepository.EksternNotifikasjonBaseEntity
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity
import no.nav.aap.util.StringExtensions.partialMask
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*
import javax.persistence.CascadeType.ALL
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

interface MinSideOppgaveRepository : MinSideRepository<Oppgave> {

    @Entity(name = "oppgave")
    @Table(name = "minsideoppgaver")
    @EntityListeners(AuditingEntityListener::class)
    class Oppgave(
            fnr: String,
            eventid: UUID,
            done: Boolean = false,
            @OneToMany(mappedBy = "oppgave", cascade = [ALL], orphanRemoval = true)
            var notifikasjoner: MutableSet<EksternOppgaveNotifikasjon> = mutableSetOf()) :
        MinSideBaseEntity(fnr = fnr, eventid = eventid, done = done) {
        override fun toString() =
            "Oppgave(fnr=${fnr.partialMask()}, created=$created, eventid=$eventid, updated=$updated, done=$done,id=$id)"
    }

    @Entity(name = "eksternoppgavenotifikasjon")
    @Table(name = "eksterneoppgavenotifikasjoner")
    @EntityListeners(AuditingEntityListener::class)
    class EksternOppgaveNotifikasjon(
            @ManyToOne(optional = false)
            var oppgave: Oppgave? = null,
            eventid: UUID,
            distribusjonid: Long,
            distribusjonkanal: String) : EksternNotifikasjonBaseEntity(eventid = eventid,
            distribusjonkanal = distribusjonkanal,
            distribusjonid = distribusjonid) {
        override fun toString() =
            "EksternOppgaveNotifikasjon(distribusjonid=$distribusjonid,distribusjondato=$distribusjondato,distribusjonkanal=$distribusjonkanal,oppgave=$oppgave,id=$id)"
    }

}