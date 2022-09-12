package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.arkiv.ArkivFordeler.ArkivResultat
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
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
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

interface SøknadRepository : JpaRepository<Søknad, Long>, JpaSpecificationExecutor<Søknad> {

    fun getSøknadByFnr(@Param("fnr") fnr: String, pageable: Pageable): List<Søknad>
    fun getSøknadByEventidAndFnr(@Param("eventid") eventId: UUID, @Param("fnr") fnr: String): Søknad?

    fun getSisteSøknad(fnr: Fødselsnummer) =
        getSøknadByFnr(fnr.fnr, PageRequest.of(0, 1, Sort.by("created").descending())).firstOrNull()

    @Entity(name = "søknad")
    @Table(name = "soknader")
    class Søknad(
            fnr: String,
            val journalpostid: String,
            eventid: UUID,
            @OneToMany(mappedBy = "soknad", cascade = [ALL], orphanRemoval = true)
            var ettersendinger: MutableSet<Ettersending> = mutableSetOf(),
            @OneToMany(mappedBy = "soknad", cascade = [ALL], orphanRemoval = true)
            var manglendevedlegg: MutableSet<ManglendeVedlegg> = mutableSetOf(),
            @OneToMany(mappedBy = "soknad", cascade = [ALL], orphanRemoval = true)
            var innsendtevedlegg: MutableSet<InnsendteVedlegg> = mutableSetOf()) : BaseEntity(fnr, eventid = eventid) {
        fun registrerSomVedlagte(vedlagte: List<VedleggType>) {
            vedlagte.forEach {
                with(InnsendteVedlegg(soknad = this, vedleggtype = it, eventid = eventid)) {
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

        private fun registrerVedlagtFraEttersending(m: ManglendeVedlegg) =
            with(m) {
                registrerSomEttersendt(this)
                manglendevedlegg.remove(this)
            }

        private fun registrerSomEttersendt(m: ManglendeVedlegg) =
            with(InnsendteVedlegg(soknad = this, vedleggtype = m.vedleggtype, eventid = eventid)) {
                innsendtevedlegg.add(this)
                soknad = this@Søknad
            }

        private fun tidligereManglendeNåEttersendte(e: List<EttersendtVedlegg>) =
            manglendevedlegg.filter { m -> e.any { m.vedleggtype == it.vedleggType } }

        fun registrerEttersending(fnr: Fødselsnummer,
                                  res: ArkivResultat,
                                  ettersendteVedlegg: List<EttersendtVedlegg>) {
            ettersendinger.add(Ettersending(fnr.fnr, res.journalpostId, soknad = this))
            tidligereManglendeNåEttersendte(ettersendteVedlegg)
                .forEach(::registrerVedlagtFraEttersending)
        }
    }

    @Entity(name = "ettersending")
    @Table(name = "ettersendinger")
    class Ettersending(
            fnr: String,
            val journalpostid: String,
            eventid: UUID = callIdAsUUID(),
            @ManyToOne
            var soknad: Søknad? = null) : BaseEntity(fnr, eventid = eventid)

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