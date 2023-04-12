package no.nav.aap.api.søknad.minside

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID
import no.nav.aap.api.søknad.minside.MinSideBeskjedRepository.Beskjed
import no.nav.aap.api.søknad.minside.MinSideRepository.EksternNotifikasjonBaseEntity
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity
import no.nav.aap.util.StringExtensions.partialMask

interface MinSideBeskjedRepository : MinSideRepository<Beskjed> {

    @Entity(name = "beskjed")
    @Table(name = "minsidebeskjeder")
    class Beskjed(fnr: String,
                  eventid: UUID,
                  @OneToMany(mappedBy = "beskjed", cascade = [ALL], orphanRemoval = true)
                  var notifikasjoner: MutableSet<EksternBeskjedNotifikasjon> = mutableSetOf()) :
        MinSideBaseEntity(fnr, eventid)  {
        override fun toString() = "${javaClass.simpleName} [fnr=${fnr.partialMask()}, created=$created, eventid=$eventid, updated=$updated,id=$id]"
    }

    @Entity(name = "eksternbeskjednotifikasjon")
    @Table(name = "eksternebeskjednotifikasjoner")
    class EksternBeskjedNotifikasjon(@ManyToOne(optional = false) var beskjed: Beskjed? = null,
                                     eventid: UUID,
                                     distribusjonid: Long,
                                     distribusjonkanal: String) :
        EksternNotifikasjonBaseEntity(eventid, distribusjonid, distribusjonkanal)
}