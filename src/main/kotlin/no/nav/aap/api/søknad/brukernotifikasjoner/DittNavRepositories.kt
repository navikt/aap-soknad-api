package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.api.søknad.brukernotifikasjoner.JPADittNavBeskjedRepository.JPADittNavBeskjed
import no.nav.aap.api.søknad.brukernotifikasjoner.JPADittNavOppgaveRepository.JPADittNavOppgave
import no.nav.aap.util.StringExtensions.partialMask
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*
import javax.persistence.AttributeConverter
import javax.persistence.Converter
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table

interface JPADittNavBeskjedRepository : JpaRepository<JPADittNavBeskjed, Long> {
    @Modifying
    @Query("update beskjeder set done = true, updated = current_timestamp where eventid = :eventid")
    fun done(@Param("eventid") eventid: UUID): Int

    @Query("select eventid from beskjeder  where fnr = :fnr and done = false and mellomlager  = true")
    fun eventIdForFnr(@Param("fnr") fnr: String): List<UUID>

    @Entity(name = "beskjeder")
    @Table(name = "dittnavbeskjeder")
    @EntityListeners(AuditingEntityListener::class)
    class JPADittNavBeskjed(
            val fnr: String,
            @CreatedDate var created: LocalDateTime? = null,
            val eventid: UUID,
            @LastModifiedDate var updated: LocalDateTime? = null,
            val done: Boolean = false,
            val mellomlager: Boolean,
            @Id @GeneratedValue(strategy = IDENTITY) val id: Long = 0) {
        override fun toString(): String =
            "JPADittNavBeskjed(fnr=${fnr.partialMask()}, mellomlager=$mellomlager, created=$created, eventid=$eventid, updated=$updated, done=$done, id=$id)"
    }
}

interface JPADittNavOppgaveRepository : JpaRepository<JPADittNavOppgave, Long> {
    @Modifying
    @Query("update oppgaver set done = true, updated = current_timestamp where eventid = :eventid")
    fun done(@Param("eventid") eventid: UUID): Int

    @Entity(name = "oppgaver")
    @Table(name = "dittnavoppgaver")
    @EntityListeners(AuditingEntityListener::class)
    class JPADittNavOppgave(
            val fnr: String,
            @CreatedDate var created: LocalDateTime? = null,
            @LastModifiedDate var updated: LocalDateTime? = null,
            var eventid: UUID,
            var done: Boolean = false,
            @Id @GeneratedValue(strategy = IDENTITY) var id: Long = 0) {
        override fun toString() =
            "JPADittNavOppgave(fnr=${fnr.partialMask()}, created=$created, updated=$updated, eventid=$eventid, done=$done, id=$id)"

    }

    @Component
    data class DittNavRepositories(val beskjeder: JPADittNavBeskjedRepository,
                                   val oppgaver: JPADittNavOppgaveRepository)

}

@Converter(autoApply = true)
class UUIDAttributeConverter : AttributeConverter<UUID, String> {
    override fun convertToDatabaseColumn(entityValue: UUID?) = entityValue?.let(UUID::toString)
    override fun convertToEntityAttribute(databaseValue: String?) = databaseValue?.let(UUID::fromString)
}