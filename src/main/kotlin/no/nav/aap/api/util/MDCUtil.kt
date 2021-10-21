package no.nav.aap.api.util

import org.slf4j.MDC
import java.util.*

object MDCUtil {
    const val NAV_CONSUMER_ID = "Nav-Consumer-Id"
    const val NAV_CALL_ID = "Nav-CallId"
    const val NAV_CALL_ID1 = "Nav-Call-Id"
    fun callId(): String {
        return MDC.get(NAV_CALL_ID) ?: UUID.randomUUID().toString()
    }

    fun consumerId(): String {
        return MDC.get(NAV_CONSUMER_ID) ?: "aap-søknad-api"
    }

    fun toMDC(key: String, value: Any?) {
        value?.let { v -> toMDC(key,v) }
    }

    fun toMDC(key: String, value: String?, defaultValue: String? = null) {
        MDC.put(key, value ?: defaultValue)
    }
}