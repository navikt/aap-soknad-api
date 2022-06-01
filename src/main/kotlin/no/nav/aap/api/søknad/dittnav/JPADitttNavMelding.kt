package no.nav.aap.api.søknad.dittnav

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "dittnavbeskjeder")
@EntityListeners(AuditingEntityListener::class)
class JPADittNavMelding(
        val fnr: String,
        @CreatedDate val opprettet: LocalDateTime? = null,
        val ref: String? = null,
        @Id @GeneratedValue(strategy = IDENTITY) val id: Long? = null)