package no.nav.aap.api.søknad.minside

import java.util.*
import javax.persistence.Entity
import javax.persistence.Table
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity
import no.nav.aap.api.søknad.minside.MinSideUtkastRepository.Utkast
import no.nav.aap.util.StringExtensions.partialMask

interface MinSideUtkastRepository : MinSideRepository<Utkast> {
    fun findByFnr(fnr: String): Utkast?

    @Entity(name = "utkast")
    @Table(name = "minsideutkast")
    class Utkast(fnr: String,eventid: UUID, var type: String, done: Boolean = false) : MinSideBaseEntity(fnr,eventid,done,false) {
        override fun toString() = "${javaClass.simpleName} [fnr=${fnr.partialMask()}, type = $type, created=$created, eventid=$eventid, updated=$updated, done=$done,id=$id]"
    }
}