package no.nav.aap.api.søknad.dittnav

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table

interface JPADittNavBeskjedRepository : JpaRepository<JPADittNavMelding, Long>

@Entity
@Table(name = "dittnavbeskjeder")
@EntityListeners(AuditingEntityListener::class)
class JPADittNavMelding(
        var fnr: String,
        @CreatedDate var created: LocalDateTime? = null,
        var ref: String? = null,
        @Id @GeneratedValue(strategy = IDENTITY) var id: Long? = null)