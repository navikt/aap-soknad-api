package no.nav.aap.api.søknad.fordeling

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
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.arkiv.ArkivClient.ArkivResultat
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.minside.MinSideOppgaveRepository.Oppgave
import no.nav.aap.api.søknad.minside.MinSideRepository.BaseEntity
import no.nav.aap.api.søknad.minside.MinSideRepository.IdentifiableTimestampedBaseEntity
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity.Companion.CREATED
import no.nav.aap.api.søknad.model.StandardEttersending.EttersendtVedlegg
import no.nav.aap.api.søknad.model.VedleggType
import no.nav.aap.util.MDCUtil.callIdAsUUID
import no.nav.aap.util.StringExtensions.partialMask
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param



interface SøknadRepository : JpaRepository<Søknad, Long> {

    @Query("select s from søknad s, ettersending e where  e.soknad.id = s.id and e.journalpostid = :journalpostid")
    fun getSøknadByEttersendingJournalpostid(journalpostid: String): Søknad?
    fun getSøknadByJournalpostid(journalpostid: String): Søknad?
    fun getSøknadByFnr(@Param("fnr") fnr: String, pageable: Pageable): List<Søknad>
    fun getSøknadByEventidAndFnr(@Param("eventid") eventId: UUID, @Param("fnr") fnr: String): Søknad?

    @Entity(name = "søknad")
    @Table(name = "soknader")
    class Søknad(
            fnr: String,
            val journalpostid: String,
            var journalpoststatus: String? = null,
            var journalfoert: LocalDateTime? = null,
            @OneToMany(mappedBy = "soknad", cascade = [ALL], orphanRemoval = true)
            var oppgaver: MutableSet<Oppgave> = mutableSetOf(),
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
             registrerManglende(manglende,eventid)

        fun registrerManglende(manglende: List<VedleggType>, eventId: UUID) =
            manglende.forEach {
                with(ManglendeVedlegg(this, eventId, it)) {
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
                                  ettersendteVedlegg: List<EttersendtVedlegg>): List<UUID> {
            ettersendinger.add(Ettersending(fnr.fnr, res.journalpostId, null,this))
            var ettersendte = tidligereManglendeNåEttersendte(ettersendteVedlegg)
               ettersendte.forEach(::registrerVedlagtFraEttersending)
            return ettersendte.map { it.eventid }
        }
        override fun toString() = "${javaClass.simpleName} [fnr=${fnr.partialMask()}, created=$created, updated=$updated, eventid=$eventid, journalpostid=$journalpostid, journalpoststatus=$journalpoststatus,id=$id)]"

    }

    @Entity(name = "ettersending")
    @Table(name = "ettersendinger")
    class Ettersending(
            fnr: String,
            val journalpostid: String,
            var journalpoststatus: String?,
            @ManyToOne
            var soknad: Søknad? = null,
            eventid: UUID = callIdAsUUID()) : BaseEntity(fnr, eventid) {
        override fun toString() = "${javaClass.simpleName} [fnr=${fnr.partialMask()}, created=$created, updated=$updated, eventid=$eventid,journalpostid=$journalpostid, journalpoststatus=$journalpoststatus, id=$id)]"

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

    companion object {
        val SISTE_SØKNAD = PageRequest.of(0, 1, Sort.by(CREATED).descending())
    }
}