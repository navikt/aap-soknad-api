package no.nav.aap.api.søknad.dittnav

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table

interface JPADittNavBeskjedRepository : JpaRepository<JPADittNavMelding, Long>
interface JPADittNavOppgaveRepository : JpaRepository<JPADittNavOppgave, Long> {
    @Modifying
    @Query("update dittnavoppgaver oppgaver set oppgaver.done =true  where oppgaver.ref =:ref")
    fun updateDone(@Param("ref") ref: String): Boolean
}

@Entity
@Table(name = "dittnavbeskjeder")
@EntityListeners(AuditingEntityListener::class)
class JPADittNavMelding(
        var fnr: String,
        @CreatedDate var created: LocalDateTime? = null,
        var ref: String? = null,
        @Id @GeneratedValue(strategy = IDENTITY) var id: Long? = null)

@Entity
@Table(name = "dittnavoppgaver")
@EntityListeners(AuditingEntityListener::class)
class JPADittNavOppgave(
        var fnr: String,
        @CreatedDate var created: LocalDateTime? = null,
        @LastModifiedDate var updated: LocalDateTime? = null,
        var ref: String? = null,
        var done: LocalDateTime? = null,
        @Id @GeneratedValue(strategy = IDENTITY) var id: Long? = null)