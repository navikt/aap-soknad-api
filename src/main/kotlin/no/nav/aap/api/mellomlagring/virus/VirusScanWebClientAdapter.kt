package no.nav.aap.api.mellomlagring.virus

import no.nav.aap.api.mellomlagring.virus.Result.FOUND
import no.nav.aap.api.mellomlagring.virus.Result.OK
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class VirusScanWebClientAdapter(@Qualifier("virus") client: WebClient, val cf: VirusScanConfig) : AbstractWebClientAdapter(client,cf) {

    override fun ping()  {
        scan(ByteArray(0), "ping")
    }

    fun scan(bytes: ByteArray, name: String?) :Result {
        if (bytes.isEmpty()) {
            log.info("Ingen scanning av null bytes")
            return OK
        }
        if (!cf.isEnabled) {
            log.warn("Scanning er deaktivert")
            return OK
        }
        log.trace("Scanner {}", name)
        val scanResult = webClient.put()
            .bodyValue(bytes)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<List<ScanResult>>()
            .block()?.single()
        log.trace("Fikk scan result {}", scanResult)
        if (FOUND == scanResult?.result) {
            log.warn("Fant virus!, status $scanResult")
            throw AttachmentVirusException(name)
        }
        log.trace("Ingen virus i  $name")
        return OK
    }
}
enum class Result { FOUND, OK }

class AttachmentVirusException(name: String?) : RuntimeException(name)
internal data class ScanResult(val filename: String, val result: Result)