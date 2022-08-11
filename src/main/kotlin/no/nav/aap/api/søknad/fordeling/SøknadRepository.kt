package no.nav.aap.api.søknad.fordeling

import com.vladmihalcea.hibernate.type.json.JsonType
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

interface SøknadRepository : JpaRepository<JPASøknad, Long>

@Entity
@Table(name = "soknader")
@EntityListeners(AuditingEntityListener::class)
@TypeDef(name = "json", typeClass = JsonType::class)
class JPASøknad(
        var fnr: String? = null,
        @Type(type = "json")
        var soknad: StandardSøknad? = null,
        @CreatedDate var created: LocalDateTime? = null,
        @LastModifiedDate var updated: LocalDateTime? = null,
        var eventid: UUID,
        @Id @GeneratedValue(strategy = IDENTITY) var id: Long? = null) {
    override fun toString() =
        "JPASøknad(fnr=${fnr?.partialMask()},søknad=$soknad, created=$created, updated=$updated, eventid=$eventid, id=$id)"
}