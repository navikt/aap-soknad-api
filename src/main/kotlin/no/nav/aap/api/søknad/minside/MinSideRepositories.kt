package no.nav.aap.api.søknad.minside

import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
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
    fun findByFnrAndDoneIsFalse(fnr: String): List<T>

    @MappedSuperclass
    abstract class MinSideBaseEntity(fnr: String, eventid: UUID, var done: Boolean) : BaseEntity(fnr, eventid = eventid)

    @MappedSuperclass
    @EntityListeners(LoggingEntityListener::class, AuditingEntityListener::class)
    abstract class BaseEntity(
            val fnr: String,
            @CreatedDate var created: LocalDateTime? = null,
            val eventid: UUID,
            @LastModifiedDate var updated: LocalDateTime? = null,
            @Id @GeneratedValue(strategy = IDENTITY)
            val id: Long = 0)

    @MappedSuperclass
    @EntityListeners(LoggingEntityListener::class, AuditingEntityListener::class)
    abstract class EksternNotifikasjonBaseEntity(
            val eventid: UUID,
            @CreatedDate
            var distribusjondato: LocalDateTime? = null,
            val distribusjonid: Long,
            val distribusjonkanal: String,
            @Id @GeneratedValue(strategy = IDENTITY)
            val id: Long = 0)
}

@Converter(autoApply = true)
class UUIDAttributeConverter : AttributeConverter<UUID, String> {
    override fun convertToDatabaseColumn(entityValue: UUID?) = entityValue?.let(UUID::toString)
    override fun convertToEntityAttribute(databaseValue: String?) = databaseValue?.let(UUID::fromString)
}

class LoggingEntityListener {

    fun name(entity: Entity) = getMergedAnnotation(entity::class.java, Entity::class.java)?.name

    @PrePersist
    private fun lagrer(entity: Entity) {
        log.trace("Lagrer ${name(entity)} $entity i DB")
    }

    @PreUpdate
    private fun oppdaterer(entity: Entity) {
        log.trace("Oppdaterer ${name(entity)} $entity i DB")
    }

    @PreRemove
    private fun fjerner(entity: Entity) {
        log.trace("Fjerner ${name(entity)} $entity fra DB")
    }

    @PostPersist
    private fun lagret(entity: Entity) {
        log.trace("Lagret ${name(entity)} $entity i DB")
    }

    @PostUpdate
    private fun oppdatert(entity: Entity) {
        log.trace("Oppdatert ${name(entity)} $entity i DB")
    }

    @PostRemove
    private fun fjernet(entity: Entity) {
        log.trace("Fjernet ${name(entity)} $entity fra DB")
    }

    @PostLoad
    private fun lest(entity: Entity) {
        log.trace("Lest ${name(entity)} $entity fra DB")
    }

    companion object {
        private val log = getLogger(LoggingEntityListener::class.java)
    }
}

@Component
data class MinSideRepositories(val beskjeder: MinSideBeskjedRepository,
                               val oppgaver: MinSideOppgaveRepository,
                               var søknader: SøknadRepository)