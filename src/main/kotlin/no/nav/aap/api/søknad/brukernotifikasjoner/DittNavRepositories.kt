package no.nav.aap.api.søknad.brukernotifikasjoner

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table

interface JPADittNavSøknadRepository : JpaRepository<JPASøknad, Long> {
    fun getByFnr(fnr: String): JPASøknad?
    fun deleteByGyldigtilBefore(dateTime: LocalDateTime?): Long
    fun deleteByFnr(fnr: String?): List<JPASøknad>?

}

interface JPADittNavBeskjedRepository : JpaRepository<JPADittNavBeskjed, Long> {
    @Modifying
    @Query("update JPADittNavBeskjed o set o.done = true, o.updated = current_timestamp where o.eventId = :eventId")
    fun done(@Param("eventId") eventId: String)
}

interface JPADittNavOppgaveRepository : JpaRepository<JPADittNavOppgave, Long> {
    @Modifying
    @Query("update JPADittNavOppgave o set o.done = true, o.updated = current_timestamp where o.eventId = :eventId")
    fun done(@Param("eventId") eventId: String)
}

@Component
data class DittNavRepositories(val beskjed: JPADittNavBeskjedRepository,
                               val oppgave: JPADittNavOppgaveRepository,
                               val søknader: JPADittNavSøknadRepository)

@Entity
@Table(name = "dittnavbeskjeder")
@EntityListeners(AuditingEntityListener::class)
class JPADittNavBeskjed(
        var fnr: String,
        @CreatedDate var created: LocalDateTime? = null,
        var eventId: String? = null,
        @LastModifiedDate var updated: LocalDateTime? = null,
        var done: Boolean? = null,
        @Id @GeneratedValue(strategy = IDENTITY) var id: Long? = null) {
    override fun toString(): String =
        "JPADittNavBeskjed(fnr='$fnr', created=$created, eventId=$eventId, updated=$updated, done=$done, id=$id)"
}

@Entity
@Table(name = "dittnavoppgaver")
@EntityListeners(AuditingEntityListener::class)
class JPADittNavOppgave(
        var fnr: String,
        @CreatedDate var created: LocalDateTime? = null,
        @LastModifiedDate var updated: LocalDateTime? = null,
        var eventId: String? = null,
        var done: Boolean? = null,
        @Id @GeneratedValue(strategy = IDENTITY) var id: Long? = null) {
    override fun toString() =
        "JPADittNavOppgave(fnr='$fnr', created=$created, updated=$updated, ref=$eventId, done=$done, id=$id)"
}

@Entity
@Table(name = "soknader")
@EntityListeners(AuditingEntityListener::class)
class JPASøknad(
        var fnr: String,
        @CreatedDate var created: LocalDateTime? = null,
        @LastModifiedDate var updated: LocalDateTime? = null,
        var eventId: String? = null,
        var gyldigtil: LocalDateTime? = null,
        @Id @GeneratedValue(strategy = IDENTITY) var id: Long? = null) {
    override fun toString() =
        "JPASøknad(fnr='$fnr', created=$created, updated=$updated, ref=$eventId, gyldigtil=$gyldigtil, id=$id)"

}