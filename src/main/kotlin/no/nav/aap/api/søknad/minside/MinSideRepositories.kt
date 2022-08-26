package no.nav.aap.api.søknad.minside

import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.minside.MinSideRepository.BaseEntity
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.AbstractPersistable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*
import javax.persistence.AttributeConverter
import javax.persistence.Converter
import javax.persistence.Entity
import javax.persistence.EntityListeners
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
    @EntityListeners(LoggingEntityListener::class)

    abstract class BaseEntity(
            val fnr: String,
            @CreatedDate var created: LocalDateTime? = null,
            val eventid: UUID,
            @LastModifiedDate var updated: LocalDateTime? = null) : AbstractPersistable<Long>() {

        fun name() = AnnotatedElementUtils.getMergedAnnotation(javaClass, Entity::class.java)

        @PrePersist
        fun logPre() {
            log.trace("Lagrer ${name()} $this i DB")
        }

        @PostPersist
        fun logPost() {
            log.trace("Lagret ${name()} $this i DB")
        }

        companion object {
            val log = getLogger(javaClass)
        }
    }

    @MappedSuperclass
    abstract class EksternNotifikasjonBaseEntity(
            val eventid: UUID,
            @CreatedDate
            var distribusjondato: LocalDateTime? = null,
            val distribusjonid: Long,
            val distribusjonkanal: String) : AbstractPersistable<Long>()
}

@Converter(autoApply = true)
class UUIDAttributeConverter : AttributeConverter<UUID, String> {
    override fun convertToDatabaseColumn(entityValue: UUID?) = entityValue?.let(UUID::toString)
    override fun convertToEntityAttribute(databaseValue: String?) = databaseValue?.let(UUID::fromString)
}

class LoggingEntityListener {
    @PrePersist
    private fun lagrer(entity: BaseEntity) {
        log.trace("Lagrer $entity i DB")
    }

    @PreUpdate
    private fun oppdaterer(entity: BaseEntity) {
        log.trace("Oppdaterer $entity i DB")
    }

    @PreRemove
    private fun fjerner(entity: BaseEntity) {
        log.trace("Fjerner $entity fra DB")
    }

    @PostPersist
    private fun lagret(entity: BaseEntity) {
        log.trace("Lagret $entity i DB")
    }

    @PostUpdate
    private fun oppdatert(entity: BaseEntity) {
        log.trace("Oppdatert $entity i DB")
    }

    @PostRemove
    private fun fjernet(entity: BaseEntity) {
        log.trace("Fjernet $entity fra DB")
    }

    @PostLoad
    private fun lest(entity: BaseEntity) {
        log.trace("Lest $entity fra DB")
    }

    companion object {
        private val log = getLogger(LoggingEntityListener::class.java)
    }
}

@Component
data class MinSideRepositories(val beskjeder: MinSideBeskjedRepository,
                               val oppgaver: MinSideOppgaveRepository,
                               var søknader: SøknadRepository)