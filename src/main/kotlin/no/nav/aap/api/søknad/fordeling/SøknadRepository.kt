package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.søknad.ettersending.Ettersending.EttersendtVedlegg
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.minside.MinSideRepository.BaseEntity
import no.nav.aap.api.søknad.minside.MinSideRepository.IdentifiableTimestampedBaseEntity
import no.nav.aap.api.søknad.model.VedleggType
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*
import javax.persistence.CascadeType.ALL
import javax.persistence.Entity
import javax.persistence.EnumType.STRING
import javax.persistence.Enumerated
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass
import javax.persistence.OneToMany
import javax.persistence.Table

interface SøknadRepository : JpaRepository<Søknad, Long> {

    fun getSøknadByFnrAndCreatedIsAfterOrderByCreatedDesc(@Param("fnr") fnr: String,
                                                          @Param("created") fra: LocalDateTime): List<Søknad>

    fun getSøknadByFnrOrderByCreatedDesc(@Param("fnr") fnr: String): List<Søknad>
    fun getSøknadByEventidAndFnr(@Param("eventid") eventId: UUID, @Param("fnr") fnr: String): Søknad?

    @Entity(name = "søknad")
    @Table(name = "soknader")
    class Søknad(
            fnr: String,
            val journalpostid: String,
            eventid: UUID,
            @OneToMany(mappedBy = "soknad", cascade = [ALL], orphanRemoval = true)
            var manglendevedlegg: MutableSet<ManglendeVedlegg> = mutableSetOf(),
            @OneToMany(mappedBy = "soknad", cascade = [ALL], orphanRemoval = true)
            var innsendtevedlegg: MutableSet<InnsendteVedlegg> = mutableSetOf(),
                ) : BaseEntity(fnr, eventid = eventid) {
        fun registrerSomVedlagte(vedlagte: List<VedleggType>) {
            vedlagte.forEach { type ->
                with(InnsendteVedlegg(soknad = this, vedleggtype = type, eventid = eventid)) {
                    innsendtevedlegg.add(this)
                    soknad = this@Søknad
                }
            }
        }

        fun registrerSomManglende(manglende: List<VedleggType>) =
            manglende.forEach { type ->
                with(ManglendeVedlegg(soknad = this, vedleggtype = type, eventid = eventid)) {
                    manglendevedlegg.add(this)
                    soknad = this@Søknad
                }
            }

        fun registrerVedlagtFraEttersending(m: ManglendeVedlegg) =
            with(m) {
                registrerSomEttersendt(this)
                manglendevedlegg.remove(this)
            }

        private fun registrerSomEttersendt(m: ManglendeVedlegg) =
            with(InnsendteVedlegg(soknad = this, vedleggtype = m.vedleggtype, eventid = eventid)) {
                innsendtevedlegg.add(this)
                soknad = this@Søknad
            }

        fun tidligereManglendeNåEttersendte(e: List<EttersendtVedlegg>) =
            manglendevedlegg.filter { m -> e.any { m.vedleggtype == it.vedleggType } }
    }

    @Entity(name = "manglendevedlegg")
    @Table(name = "manglendevedlegg")
    class ManglendeVedlegg(
            @ManyToOne(optional = false)
            var soknad: Søknad? = null,
            eventid: UUID,
            vedleggtype: VedleggType) : VedleggBaseEntity(eventid, vedleggtype)

    @Entity(name = "innsendtevedlegg")
    @Table(name = "innsendtevedlegg")
    class InnsendteVedlegg(
            @ManyToOne(optional = false)
            var soknad: Søknad? = null,
            eventid: UUID,
            vedleggtype: VedleggType) : VedleggBaseEntity(eventid, vedleggtype)

    @MappedSuperclass
    abstract class VedleggBaseEntity(
            val eventid: UUID,
            @Enumerated(STRING)
            val vedleggtype: VedleggType,
            @LastModifiedDate var updated: LocalDateTime? = null) : IdentifiableTimestampedBaseEntity() {
        override fun toString() =
            "${javaClass.simpleName} [created=$created, updated=$updated, eventid=$eventid, vedleggType=$vedleggtype, id=$id)]"

    }
}