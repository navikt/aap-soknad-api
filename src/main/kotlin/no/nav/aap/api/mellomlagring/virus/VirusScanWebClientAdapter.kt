package no.nav.aap.api.mellomlagring.virus

import no.nav.aap.api.mellomlagring.virus.Result.OK
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
        scan(ByteArray(0), "ping")
    }

    fun scan(bytes: ByteArray, name: String?) : Result {
        if (skalScanne(bytes, cf)) {
            log.trace("Ingen scanning av (${bytes.size} bytes, enabled=${cf.enabled})")
            return OK
        }
        log.trace("Scanner {}", name)
        return webClient
            .put()
            .bodyValue(bytes)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<List<ScanResult>>()
            .block()
            ?.single().also { log.trace("Fikk scan result $it") }?.result ?: OK
    }
    private fun skalScanne(bytes: ByteArray, cf: VirusScanConfig) = bytes.isEmpty() || !cf.isEnabled
}
enum class Result { FOUND, OK }

class AttachmentVirusException(name: String?) : RuntimeException(name)
internal data class ScanResult(val filename: String, val result: Result)