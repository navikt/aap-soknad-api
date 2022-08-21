package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavBeskjedRepository.Beskjed
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavNotifikasjonRepository.EksternNotifikasjon
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavOppgaveRepository.Oppgave
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
import javax.persistence.CascadeType.ALL
import javax.persistence.Converter
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

interface DittNavBeskjedRepository : JpaRepository<Beskjed, Long> {
    @Modifying
    @Query("update beskjed set done = true, updated = current_timestamp where eventid = :eventid")
    fun done(@Param("eventid") eventid: UUID): Int

    @Query("select eventid from beskjed where  done = false and fnr = :fnr")
    fun allNotDone(@Param("fnr") fnr: String): List<UUID>

    @Modifying
    @Query("update beskjed set distribusjondato = current_timestamp, distribusjonkanal = :distribusjonkanal, distribusjonid = :distribusjonid, updated = current_timestamp where eventid = :eventid")
    fun distribuert(@Param("eventid") eventid: UUID,
                    @Param("distribusjonkanal") distribusjonkanal: String,
                    @Param("distribusjonid") distribusjonid: Long): Int

    @Entity(name = "beskjed")
    @Table(name = "dittnavbeskjeder")
    @EntityListeners(AuditingEntityListener::class)
    class Beskjed(
            val fnr: String,
            @CreatedDate var created: LocalDateTime? = null,
            val eventid: UUID,
            @LastModifiedDate var updated: LocalDateTime? = null,
            val done: Boolean = false,
            val distribusjondato: LocalDateTime? = null,
            val distribusjonid: Long? = null,
            val distribusjonkanal: String? = null,
            @Id @GeneratedValue(strategy = IDENTITY) val id: Long = 0) {
        override fun toString(): String =
            "Beskjed(fnr=${fnr.partialMask()}, created=$created, eventid=$eventid, updated=$updated, done=$done, distribusjonid=$distribusjonid,distribusjondato=$distribusjondato,distribusjonkanal=$distribusjonkanal,id=$id)"
    }
}

interface DittNavOppgaveRepository : JpaRepository<Oppgave, Long> {
    fun findOppgaveByEventid(eventid: UUID): Oppgave?

    @Modifying
    @Query("update oppgave set done = true, updated = current_timestamp where eventid = :eventid")
    fun done(@Param("eventid") eventid: UUID): Int

    @Query("select eventid from oppgave where  done = false and fnr = :fnr")
    fun allNotDone(@Param("fnr") fnr: String): List<UUID>

    @Modifying
    @Query("update oppgave set distribusjondato = current_timestamp, distribusjonkanal = :distribusjonkanal, distribusjonid = :distribusjonid, updated = current_timestamp where eventid = :eventid")
    fun distribuert(@Param("eventid") eventid: UUID,
                    @Param("distribusjonkanal") distribusjonkanal: String,
                    @Param("distribusjonid") distribusjonid: Long): Int

    @Entity(name = "oppgave")
    @Table(name = "dittnavoppgaver")
    @EntityListeners(AuditingEntityListener::class)
    class Oppgave(
            val fnr: String,
            @CreatedDate var created: LocalDateTime? = null,
            @LastModifiedDate var updated: LocalDateTime? = null,
            @OneToMany(mappedBy = "oppgave", cascade = [ALL])
            var notifikasjoner: Set<EksternNotifikasjon> = setOf(),
            val eventid: UUID,
            val done: Boolean = false,
            val distribusjondato: LocalDateTime? = null,
            val distribusjonid: Long? = null,
            val distribusjonkanal: String? = null,
            @Id @GeneratedValue(strategy = IDENTITY) var id: Long = 0) {
        override fun toString(): String =
            "Oppgave(fnr=${fnr.partialMask()}, created=$created, eventid=$eventid, updated=$updated, done=$done, distribusjonid=$distribusjonid,distribusjondato=$distribusjondato,distribusjonkanal=$distribusjonkanal,id=$id)"
    }
}

@Component
data class DittNavRepositories(val beskjeder: DittNavBeskjedRepository,
                               val oppgaver: DittNavOppgaveRepository,
                               var notifikasjoner: DittNavNotifikasjonRepository)

@Converter(autoApply = true)
class UUIDAttributeConverter : AttributeConverter<UUID, String> {
    override fun convertToDatabaseColumn(entityValue: UUID?) = entityValue?.let(UUID::toString)
    override fun convertToEntityAttribute(databaseValue: String?) = databaseValue?.let(UUID::fromString)
}

interface DittNavNotifikasjonRepository : JpaRepository<EksternNotifikasjon, Long> {

    @Entity(name = "eksternnotifikasjon")
    @Table(name = "eksternenotifikasjoner")
    @EntityListeners(AuditingEntityListener::class)
    class EksternNotifikasjon(
            @ManyToOne
            @JoinColumn(name = "eventid", nullable = false)
            var oppgave: Oppgave? = null,
            @CreatedDate
            var distribusjondato: LocalDateTime? = null,
            val distribusjonid: Long? = null,
            val distribusjonkanal: String? = null,
            @Id @GeneratedValue(strategy = IDENTITY) var id: Long = 0) {
        override fun toString(): String =
            "EksterneNotifikasjoner(distribusjonid=$distribusjonid,distribusjondato=$distribusjondato,distribusjonkanal=$distribusjonkanal,id=$id)"
    }
}