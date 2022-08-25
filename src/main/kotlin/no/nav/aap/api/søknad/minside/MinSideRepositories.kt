package no.nav.aap.api.søknad.minside

import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity
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
import javax.persistence.MappedSuperclass

@NoRepositoryBean
interface MinSideRepository<T : MinSideBaseEntity> : JpaRepository<T, Long> {
    fun findByEventid(eventid: UUID): T?
    fun findByFnrAndDoneIsFalse(fnr: String): List<T>

    @MappedSuperclass
    abstract class MinSideBaseEntity(fnr: String, eventid: UUID, var done: Boolean) : BaseEntity(fnr, eventid = eventid)

    @MappedSuperclass
    abstract class BaseEntity(
            val fnr: String,
            @CreatedDate var created: LocalDateTime? = null,
            val eventid: UUID,
            @LastModifiedDate var updated: LocalDateTime? = null) : AbstractPersistable<Long>()

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

@Component
data class MinSideRepositories(val beskjeder: MinSideBeskjedRepository,
                               val oppgaver: MinSideOppgaveRepository,
                               var søknader: SøknadRepository)