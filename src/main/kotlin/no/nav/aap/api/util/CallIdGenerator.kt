package no.nav.aap.api.util

import org.springframework.stereotype.Component
import java.util.*

object CallIdGenerator {
    fun create(): String {
        return UUID.randomUUID().toString()
    }
}