package no.nav.aap.api.søknad.minside

import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.partialMask
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*
import javax.persistence.AttributeConverter
import javax.persistence.Converter
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.PostLoad
import javax.persistence.PostPersist
import javax.persistence.PostRemove
import javax.persistence.PostUpdate
import javax.persistence.PrePersist
import javax.persistence.PreRemove
import javax.persistence.PreUpdate

@NoRepositoryBean
interface MinSideRepository<T : MinSideBaseEntity> : JpaRepository<T, Long> {
    fun findByEventid(eventid: UUID): T?
    fun findByFnrAndEventid(fnr: String, eventid: UUID): T?
    fun findByFnrAndDoneIsFalse(fnr: String): List<T>

    @MappedSuperclass
    abstract class MinSideBaseEntity(fnr: String, eventid: UUID, var done: Boolean) : BaseEntity(fnr, eventid) {
        override fun toString() =
            "${javaClass.simpleName} [fnr=${fnr.partialMask()}, created=$created, eventid=$eventid, updated=$updated, done=$done,id=$id]"
    }

    @MappedSuperclass
    abstract class BaseEntity(val fnr: String,
                              val eventid: UUID,
                              @LastModifiedDate var updated: LocalDateTime? = null) :
        IdentifiableTimestampedBaseEntity() {
        override fun toString() =
            "${javaClass.simpleName} [fnr=${fnr.partialMask()}, created=$created, updated=$updated, eventid=$eventid, id=$id)]"
    }

    @MappedSuperclass
    abstract class EksternNotifikasjonBaseEntity(
            val eventid: UUID,
            val distribusjonid: Long,
            val distribusjonkanal: String) : IdentifiableTimestampedBaseEntity() {
        override fun toString() =
            "${javaClass.simpleName} [(distribusjonid=$distribusjonid,created=$created,distribusjonkanal=$distribusjonkanal,id=$id]"
    }

    @MappedSuperclass
    @EntityListeners(LoggingEntityListener::class, AuditingEntityListener::class)
    abstract class IdentifiableTimestampedBaseEntity(
            @CreatedDate
            var created: LocalDateTime? = null,
            @Id @GeneratedValue(strategy = IDENTITY)
            val id: Long = 0)

}

@Converter(autoApply = true)
class UUIDAttributeConverter : AttributeConverter<UUID, String> {
    override fun convertToDatabaseColumn(entityValue: UUID?) = entityValue?.let(UUID::toString)
    override fun convertToEntityAttribute(databaseValue: String?) = databaseValue?.let(UUID::fromString)
}

class LoggingEntityListener {

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
                               var søknader: SøknadRepository)