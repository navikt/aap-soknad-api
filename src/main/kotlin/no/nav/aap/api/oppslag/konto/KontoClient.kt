package no.nav.aap.api.oppslag.konto

import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class KontoClient(private val adapter: KontoWebClientAdapter) {
    val log = LoggerUtil.getLogger(javaClass)

    fun kontoInfo() = try  {
        adapter.kontoInfo()
    }
    catch (e: Exception)  {
        log.warn("OOPS",e)
        null
    }
}