package no.nav.aap.api.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LoggerUtil {
    fun getSecureLogger(): Logger = LoggerFactory.getLogger("secure")
    fun getLogger(forClass: Class<*>): Logger = LoggerFactory.getLogger(forClass)
}