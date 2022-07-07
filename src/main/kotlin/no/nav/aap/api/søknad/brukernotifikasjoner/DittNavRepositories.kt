package no.nav.aap.api.søknad.brukernotifikasjoner

import org.springframework.data.annotation.CreatedBy
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
    @Query("update JPADittNavBeskjed o set o.done = true, o.updated = current_timestamp where o.eventid = :eventid")
    fun done(@Param("eventid") eventid: String)
}

interface JPADittNavOppgaveRepository : JpaRepository<JPADittNavOppgave, Long> {
    @Modifying
    @Query("update JPADittNavOppgave o set o.done = true, o.updated = current_timestamp where o.eventid = :eventid")
    fun done(@Param("eventid") eventid: String)
}

@Component
data class DittNavRepositories(val beskjeder: JPADittNavBeskjedRepository,
                               val oppgaver: JPADittNavOppgaveRepository,
                               val søknader: JPADittNavSøknadRepository)

@Entity
@Table(name = "dittnavbeskjeder")
@EntityListeners(AuditingEntityListener::class)
class JPADittNavBeskjed(
        /*@CreatedBy */var fnr: String? = null,
                       @CreatedDate var created: LocalDateTime? = null,
                       var eventid: String,
                       @LastModifiedDate var updated: LocalDateTime? = null,
                       var done: Boolean = false,
                       @Id @GeneratedValue(strategy = IDENTITY) var id: Long? = null) {
    override fun toString(): String =
        "JPADittNavBeskjed(fnr='$fnr', created=$created, eventid=$eventid, updated=$updated, done=$done, id=$id)"
}

@Entity
@Table(name = "dittnavoppgaver")
@EntityListeners(AuditingEntityListener::class)
class JPADittNavOppgave(
        /*@CreatedBy*/ var fnr: String? = null,
                       @CreatedDate var created: LocalDateTime? = null,
                       @LastModifiedDate var updated: LocalDateTime? = null,
                       var eventid: String,
                       var done: Boolean = false,
                       @Id @GeneratedValue(strategy = IDENTITY) var id: Long? = null) {
    override fun toString() =
        "JPADittNavOppgave(fnr='$fnr', created=$created, updated=$updated, eventid=$eventid, done=$done, id=$id)"
}

@Entity
@Table(name = "soknader")
@EntityListeners(AuditingEntityListener::class)
class JPASøknad(
        @CreatedBy var fnr: String? = null,
        @CreatedDate var created: LocalDateTime? = null,
        @LastModifiedDate var updated: LocalDateTime? = null,
        var eventid: String,
        var gyldigtil: LocalDateTime? = null,
        @Id @GeneratedValue(strategy = IDENTITY) var id: Long? = null) {
    override fun toString() =
        "JPASøknad(fnr='$fnr', created=$created, updated=$updated, eventid=$eventid, gyldigtil=$gyldigtil, id=$id)"

}