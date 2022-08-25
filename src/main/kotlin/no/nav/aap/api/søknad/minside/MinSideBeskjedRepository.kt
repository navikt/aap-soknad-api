package no.nav.aap.api.søknad.minside

import no.nav.aap.api.søknad.minside.MinSideBeskjedRepository.Beskjed
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

interface MinSideBeskjedRepository : MinSideRepository<Beskjed> {

    @Entity(name = "beskjed")
    @Table(name = "minsidebeskjeder")
    @EntityListeners(AuditingEntityListener::class)
    class Beskjed(
            fnr: String,
            eventid: UUID,
            done: Boolean = false,
            @OneToMany(mappedBy = "beskjed", cascade = [ALL], orphanRemoval = true)
            var notifikasjoner: MutableSet<EksternBeskjedNotifikasjon> = mutableSetOf()) :
        MinSideBaseEntity(fnr = fnr, eventid = eventid, done = done) {
        override fun toString() =
            "Beskjed(fnr=${fnr.partialMask()}, created=$created, eventid=$eventid, updated=$updated, done=$done,id=$id)"
    }

    @Entity(name = "eksternbeskjednotifikasjon")
    @Table(name = "eksternebeskjednotifikasjoner")
    @EntityListeners(AuditingEntityListener::class)
    class EksternBeskjedNotifikasjon(
            @ManyToOne(optional = false)
            var beskjed: Beskjed? = null,
            eventid: UUID,
            distribusjonid: Long,
            distribusjonkanal: String) : EksternNotifikasjonBaseEntity(eventid = eventid,
            distribusjonkanal = distribusjonkanal,
            distribusjonid = distribusjonid) {
        override fun toString() =
            "EksternOppgaveNotifikasjon(distribusjonid=$distribusjonid,distribusjondato=$distribusjondato,distribusjonkanal=$distribusjonkanal,beskjed=$beskjed,id=$id)"
    }
}