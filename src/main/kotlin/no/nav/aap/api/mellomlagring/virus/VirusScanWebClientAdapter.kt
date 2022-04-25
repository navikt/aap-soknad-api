package no.nav.aap.api.mellomlagring.virus

import no.nav.aap.api.mellomlagring.virus.ScanResult.Result.FOUND
import no.nav.aap.api.mellomlagring.virus.ScanResult.Result.OK
import no.nav.aap.api.mellomlagring.virus.VirusScanConfig.Companion.VIRUS
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class VirusScanWebClientAdapter(@Qualifier(VIRUS) client: WebClient, val cf: VirusScanConfig) : AbstractWebClientAdapter(client,cf) {
    override fun ping()  {
        harVirus(ByteArray(0), "ping")
    }

    fun harVirus(bytes: ByteArray, name: String?) : Boolean {
        if (skalIkkeScanne(bytes, cf)) {
            log.trace("Ingen scanning av (${bytes.size} bytes, enabled=${cf.enabled})")
            return false
        }
        log.trace("Scanner {}", name)
        return when(webClient
            .put()
            .bodyValue(bytes)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<List<ScanResult>>()
            .block()
            ?.single()
            .also { log.trace("Fikk scan result $it") }
            ?.result)  {
            OK, null-> false
            FOUND -> true
        }
    }
    private fun skalIkkeScanne(bytes: ByteArray, cf: VirusScanConfig) = bytes.isEmpty() || !cf.isEnabled
}

class AttachmentException(name: String?) : RuntimeException(name)
data class ScanResult(val filename: String, val result: Result) {
    enum class Result { FOUND, OK }
}