package no.nav.aap.api.søknad.fordeling

import com.vladmihalcea.hibernate.type.json.JsonType
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.util.StringExtensions.partialMask
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
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
import javax.persistence.Table

interface SøknadRepository : JpaRepository<Søknad, Long> {
    @Entity(name = "søknad")
    @Table(name = "soknader")
    @EntityListeners(AuditingEntityListener::class)
    @TypeDef(name = "json", typeClass = JsonType::class)
    class Søknad(
            val fnr: String,
            @Type(type = "json")
            val soknad: StandardSøknad,
            @CreatedDate var created: LocalDateTime? = null,
            @LastModifiedDate var updated: LocalDateTime? = null,
            val eventid: UUID,
            @Id @GeneratedValue(strategy = IDENTITY) var id: Long = 0) {
        override fun toString() =
            "JPASøknad(fnr=${fnr.partialMask()},søknad=$soknad, created=$created, updated=$updated, eventid=$eventid, id=$id)"
    }
}