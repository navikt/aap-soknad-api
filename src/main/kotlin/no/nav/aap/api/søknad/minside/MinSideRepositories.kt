package no.nav.aap.api.søknad.minside

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.IDENTITY
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.PostRemove
import jakarta.persistence.PostUpdate
import jakarta.persistence.PrePersist
import jakarta.persistence.PreRemove
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Component
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.partialMask

@NoRepositoryBean
interface MinSideRepository<T : MinSideBaseEntity> : JpaRepository<T, Long> {
    fun findByEventid(eventid: UUID): T?
    fun deleteByFnrAndEventid(fnr: String, eventid: UUID)

    @MappedSuperclass
    abstract class MinSideBaseEntity(fnr: String, eventid: UUID) : BaseEntity(fnr, eventid) {
        override fun toString() = "${javaClass.simpleName} [fnr=${fnr.partialMask()}, created=$created, eventid=$eventid, updated=$updated, id=$id]"

        companion object{
            const val CREATED = "created"
        }
    }

    @MappedSuperclass
    abstract class BaseEntity(val fnr: String,  val eventid: UUID, @LastModifiedDate var updated: LocalDateTime? = null) : IdentifiableTimestampedBaseEntity() {
        override fun toString() = "${javaClass.simpleName} [fnr=${fnr.partialMask()}, created=$created, updated=$updated, eventid=$eventid, id=$id)]"
    }

    @MappedSuperclass
    abstract class EksternNotifikasjonBaseEntity(val eventid: UUID, val distribusjonid: Long, val distribusjonkanal: String) : IdentifiableTimestampedBaseEntity() {
        override fun toString() = "${javaClass.simpleName} [(distribusjonid=$distribusjonid,created=$created,distribusjonkanal=$distribusjonkanal,id=$id]"
    }

    @MappedSuperclass
    @EntityListeners(LoggingEntityListener::class, AuditingEntityListener::class)
    abstract class IdentifiableTimestampedBaseEntity(@CreatedDate var created: LocalDateTime? = null, @Id @GeneratedValue(strategy = IDENTITY) val id: Long = 0)

}

@Converter(autoApply = true)
class UUIDAttributeConverter : AttributeConverter<UUID, String> {
    override fun convertToDatabaseColumn(entityValue: UUID?) = entityValue?.let(UUID::toString)
    override fun convertToEntityAttribute(databaseValue: String?) = databaseValue?.let(UUID::fromString)
}

private class LoggingEntityListener {

    @PrePersist
    private fun lagrer(entity: Any) = log.trace("Lagrer $entity i DB")

    @PreUpdate
    private fun oppdaterer(entity: Any) = log.trace("Oppdaterer $entity i DB")

    @PreRemove
    private fun fjerner(entity: Any) = log.trace("Fjerner $entity fra DB")

    @PostPersist
    private fun lagret(entity: Any) = log.trace("Lagret $entity i DB")

    @PostUpdate
    private fun oppdatert(entity: Any) = log.trace("Oppdaterte $entity i DB")

    @PostRemove
    private fun fjernet(entity: Any) = log.trace("Fjernet $entity fra DB")

    @PostLoad
    private fun lest(entity: Any) = log.trace("Leste $entity fra DB")

    companion object {
        private val log = getLogger(LoggingEntityListener::class.java)
    }
}

@Component
data class MinSideRepositories(val beskjeder: MinSideBeskjedRepository,
                               val oppgaver: MinSideOppgaveRepository,
                               val utkast: MinSideUtkastRepository,
                               var søknader: SøknadRepository)