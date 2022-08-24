package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.model.VedleggType
import no.nav.aap.util.StringExtensions.partialMask
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*
import javax.persistence.CascadeType.ALL
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.EnumType.STRING
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

interface SøknadRepository : JpaRepository<Søknad, Long> {

    fun getSøknadByFnrOrderByCreatedDesc(@Param("fnr") fnr: String): List<Søknad>?

    @Entity(name = "søknad")
    @Table(name = "soknader")
    @EntityListeners(AuditingEntityListener::class)
    class Søknad(
            val fnr: String,
            val journalpostid: String,
            @OneToMany(mappedBy = "soknad", cascade = [ALL], orphanRemoval = true)
            var manglendevedlegg: MutableSet<ManglendeVedlegg> = mutableSetOf(),
            @CreatedDate var created: LocalDateTime? = null,
            @LastModifiedDate var updated: LocalDateTime? = null,
            val eventid: UUID,
            @Id @GeneratedValue(strategy = IDENTITY) var id: Long = 0) {
        override fun toString() =
            "JPASøknad(fnr=${fnr.partialMask()}, created=$created, updated=$updated, eventid=$eventid, id=$id)"
    }

    @Entity(name = "manglendevedlegg")
    @Table(name = "manglendevedlegg")
    @EntityListeners(AuditingEntityListener::class)
    class ManglendeVedlegg(
            @CreatedDate var created: LocalDateTime? = null,
            @LastModifiedDate var updated: LocalDateTime? = null,
            @ManyToOne(optional = false)
            var soknad: Søknad? = null,
            val eventid: UUID,
            @Enumerated(STRING)
            val vedleggtype: VedleggType,
            @Id @GeneratedValue(strategy = IDENTITY) var id: Long = 0) {
        override fun toString() =
            "ManglendeVedlegg(created=$created, updated=$updated, eventid=$eventid, id=$id)"
    }
}