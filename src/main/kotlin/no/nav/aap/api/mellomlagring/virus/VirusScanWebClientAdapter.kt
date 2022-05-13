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
class VirusScanWebClientAdapter(@Qualifier(VIRUS) client: WebClient, val cf: VirusScanConfig) :
    AbstractWebClientAdapter(client, cf) {
    override fun ping() {
        log.trace("pinger")
        if (harVirus(byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D), "ping")) {
            log.trace("ping feilet")
            throw AttachmentException("Virus ble funnet")
        }
        else {
            log.trace("ping OK")
        }
    }

    fun harVirus(bytes: ByteArray, name: String?): Boolean {
        if (skalIkkeScanne(bytes, cf)) {
            log.trace("Ingen scanning av (${bytes.size} bytes, enabled=${cf.enabled})")
            return false
        }
        log.trace("Scanner {}", name)
        return when (webClient
            .put()
            .bodyValue(bytes)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<List<ScanResult>>()
            .doOnError { t: Throwable -> log.warn("Virus-respons feilet, antar likevel OK", t) }
            .doOnSuccess { log.trace("Virus respons OK") }
            .onErrorReturn(listOf(ScanResult(name, OK)))
            .defaultIfEmpty(listOf(ScanResult(name, OK)))
            .block()
            ?.single()
            .also { log.trace("Fikk scan result $it") }
            ?.result) {
            OK, null -> false
            FOUND -> true
        }
    }

    private fun skalIkkeScanne(bytes: ByteArray, cf: VirusScanConfig) = bytes.isEmpty() || !cf.isEnabled
}

class AttachmentException(msg: String?) : RuntimeException(msg)
private data class ScanResult(val filename: String? = null, val result: Result) {
    enum class Result {
        FOUND,
        OK
    }
}