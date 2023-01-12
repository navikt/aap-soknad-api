package no.nav.aap.api.søknad.minside

import java.util.*
import javax.persistence.CascadeType.ALL
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table
import no.nav.aap.api.søknad.minside.MinSideBeskjedRepository.Beskjed
import no.nav.aap.api.søknad.minside.MinSideRepository.EksternNotifikasjonBaseEntity
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity
import no.nav.aap.util.StringExtensions.partialMask

interface MinSideBeskjedRepository : MinSideRepository<Beskjed> {

    @Entity(name = "beskjed")
    @Table(name = "minsidebeskjeder")
    class Beskjed(fnr: String,
                  eventid: UUID,
                  ekstern: Boolean = false,
                  @OneToMany(mappedBy = "beskjed", cascade = [ALL], orphanRemoval = true)
                  var notifikasjoner: MutableSet<EksternBeskjedNotifikasjon> = mutableSetOf()) :
        MinSideBaseEntity(fnr, eventid,ekstern)  {
        override fun toString() = "${javaClass.simpleName} [fnr=${fnr.partialMask()}, created=$created, eventid=$eventid, updated=$updated,ekstern=$ekstern,id=$id]"
    }

    @Entity(name = "eksternbeskjednotifikasjon")
    @Table(name = "eksternebeskjednotifikasjoner")
    class EksternBeskjedNotifikasjon(@ManyToOne(optional = false) var beskjed: Beskjed? = null,
                                     eventid: UUID,
                                     distribusjonid: Long,
                                     distribusjonkanal: String) :
        EksternNotifikasjonBaseEntity(eventid, distribusjonid, distribusjonkanal)
}