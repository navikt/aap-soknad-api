package no.nav.aap.api.søknad.util

import org.slf4j.MDC
import java.util.Optional

object MDCUtil {
    const val NAV_CONSUMER_ID = "Nav-Consumer-Id"
    const val NAV_CALL_ID = "Nav-CallId"
    const val NAV_CALL_ID1 = "Nav-Call-Id"
    fun callId(): String {
        return MDC.get(NAV_CALL_ID)
    }

    fun consumerId(): String {
        return MDC.get(NAV_CONSUMER_ID)
    }

    fun toMDC(key: String?, value: Any?) {
        Optional.ofNullable(value).ifPresent { v: Any? -> toMDC(key, v) }
    }

    @JvmOverloads
    fun toMDC(key: String?, value: String?, defaultValue: String? = null) {
        MDC.put(key, Optional.ofNullable(value).orElse(defaultValue))
    }
}