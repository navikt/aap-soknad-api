package no.nav.aap.api.søknad.fordeling

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Entity
import jakarta.persistence.EnumType.*
import jakarta.persistence.Enumerated
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.arkiv.ArkivClient.ArkivResultat
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.minside.MinSideRepository.BaseEntity
import no.nav.aap.api.søknad.minside.MinSideRepository.IdentifiableTimestampedBaseEntity
import no.nav.aap.api.søknad.model.StandardEttersending.EttersendtVedlegg
import no.nav.aap.api.søknad.model.VedleggType
import no.nav.aap.util.MDCUtil.callIdAsUUID
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param

interface SøknadRepository : JpaRepository<Søknad, Long> {

    fun getSøknadByJournalpostid(journalpostid: String): Søknad?
    fun getSøknadByFnr(@Param("fnr") fnr: String, pageable: Pageable): List<Søknad>
    fun getSøknadByEventidAndFnr(@Param("eventid") eventId: UUID, @Param("fnr") fnr: String): Søknad?

    @Entity(name = "søknad")
    @Table(name = "soknader")
    class Søknad(
            fnr: String,
            val journalpostid: String,
            var journalfoert: LocalDateTime? = null,
            eventid: UUID = callIdAsUUID(),
            @OneToMany(mappedBy = "soknad", cascade = [ALL], orphanRemoval = true)
            var ettersendinger: MutableSet<Ettersending> = mutableSetOf(),
            @OneToMany(mappedBy = "soknad", cascade = [ALL], orphanRemoval = true)
            var manglendevedlegg: MutableSet<ManglendeVedlegg> = mutableSetOf(),
            @OneToMany(mappedBy = "soknad", cascade = [ALL], orphanRemoval = true)
            var innsendtevedlegg: MutableSet<InnsendteVedlegg> = mutableSetOf()) : BaseEntity(fnr, eventid) {
        fun registrerVedlagte(vedlagte: List<VedleggType>) {
            vedlagte.forEach {
                with(InnsendteVedlegg(this, eventid, it)) {
                    innsendtevedlegg.add(this)
                    soknad = this@Søknad
                }
            }
        }

        fun registrerManglende(manglende: List<VedleggType>) =
            manglende.forEach {
                with(ManglendeVedlegg(this, eventid, it)) {
                    manglendevedlegg.add(this)
                    soknad = this@Søknad
                }
            }

        private fun registrerVedlagtFraEttersending(m: ManglendeVedlegg) =
            with(m) {
                registrerSomEttersendt(this)
                manglendevedlegg.remove(this)
            }

        private fun registrerSomEttersendt(m: ManglendeVedlegg) =
            with(InnsendteVedlegg(this, eventid, m.vedleggtype)) {
                innsendtevedlegg.add(this)
                soknad = this@Søknad
            }

        private fun tidligereManglendeNåEttersendte(e: List<EttersendtVedlegg>) =
            manglendevedlegg.filter { m -> e.any { m.vedleggtype == it.vedleggType } }

        fun registrerEttersending(fnr: Fødselsnummer,
                                  res: ArkivResultat,
                                  ettersendteVedlegg: List<EttersendtVedlegg>) {
            ettersendinger.add(Ettersending(fnr.fnr, res.journalpostId, this))
            tidligereManglendeNåEttersendte(ettersendteVedlegg)
                .forEach(::registrerVedlagtFraEttersending)
        }
    }

    @Entity(name = "ettersending")
    @Table(name = "ettersendinger")
    class Ettersending(
            fnr: String,
            val journalpostid: String,
            @ManyToOne
            var soknad: Søknad? = null,
            eventid: UUID = callIdAsUUID()) : BaseEntity(fnr, eventid)

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

    companion object {
        val SISTE_SØKNAD = PageRequest.of(0, 1, Sort.by("created").descending())
    }
}