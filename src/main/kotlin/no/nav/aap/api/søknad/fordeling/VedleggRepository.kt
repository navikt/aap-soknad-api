package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.fordeling.VedleggRepository.ManglendeVedlegg
import no.nav.aap.api.søknad.model.VedleggType
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

interface VedleggRepository : JpaRepository<ManglendeVedlegg, Long> {
    @Entity(name = "manglendevedlegg")
    @Table(name = "manglendevedlegg")
    @EntityListeners(AuditingEntityListener::class)
    class ManglendeVedlegg(
            @CreatedDate var created: LocalDateTime? = null,
            @LastModifiedDate var updated: LocalDateTime? = null,
            @ManyToOne(optional = false)
            var soknad: Søknad? = null,
            val eventid: UUID,
            val vedleggtype: VedleggType,
            @Id @GeneratedValue(strategy = IDENTITY) var id: Long = 0) {
        override fun toString() =
            "ManglendeVedlegg(created=$created, updated=$updated, eventid=$eventid, id=$id)"
    }
}