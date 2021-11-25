package no.nav.aap.api.util

import java.util.*

object CallIdGenerator {
    fun create() =  UUID.randomUUID().toString()

}