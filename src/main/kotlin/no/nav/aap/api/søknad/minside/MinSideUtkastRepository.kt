package no.nav.aap.api.søknad.minside

import jakarta.persistence.Entity
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.util.*
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity
import no.nav.aap.api.søknad.minside.MinSideUtkastRepository.Utkast
import no.nav.aap.util.StringExtensions.partialMask
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface EventIdView {
    val eventid: UUID
}
interface MinSideUtkastRepository : MinSideRepository<Utkast> {
    fun existsByFnrAndSkjematype(fnr: String, skjemaType: SkjemaType): Boolean
    fun deleteByEventid(eventid: UUID)
    fun findByFnrAndSkjematype(fnr: String, skjemaType: SkjemaType): EventIdView?



    @Query("update utkast u set u.type = :type, u.updated = CURRENT_TIMESTAMP   where u.fnr = :fnr and u.eventid = :eventid")
    @Modifying
    fun oppdaterUtkast(@Param("type") type: UtkastType, @Param("fnr") fnr: String, @Param("eventid") eventid: UUID)


    @Entity(name = "utkast")
    @Table(name = "minsideutkast")
    class Utkast(fnr: String, eventid: UUID,
                 @Enumerated(STRING) var type: UtkastType,
                 @Enumerated(STRING) var skjematype: SkjemaType = STANDARD) : MinSideBaseEntity(fnr,eventid) {
        override fun toString() = "${javaClass.simpleName} [fnr=${fnr.partialMask()}, skjemaType = $skjematype, type = $type, created=$created, eventid=$eventid, updated=$updated,id=$id]"
    }
}