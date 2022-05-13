package no.nav.aap.api.mellomlagring.virus

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.aap.api.mellomlagring.virus.ScanResult.Companion.FEIL
import no.nav.aap.api.mellomlagring.virus.ScanResult.Result.FOUND
import no.nav.aap.api.mellomlagring.virus.ScanResult.Result.NONE
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
    override fun ping() =
        when (harVirus(PDF).result) {
            NONE -> throw AttachmentException("Uventet ping respons ${NONE.name}")
            FOUND, OK -> Unit
        }

    fun harVirus(bytes: ByteArray): ScanResult {
        if (skalIkkeScanne(bytes, cf)) {
            return ScanResult(NONE).also {
                log.trace("Ingen scanning av (${bytes.size} bytes, enabled=${cf.enabled})")
            }
        }
        return webClient
            .put()
            .bodyValue(bytes)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<List<ScanResult>>()
            .doOnError { t: Throwable -> log.warn("Virus-respons feilet", t) }
            .doOnSuccess { log.trace("Virus respons OK") }
            .onErrorReturn(FEIL)
            .defaultIfEmpty(FEIL)
            .block()
            ?.single()
            .also { log.trace("Fikk scan result $it") }
            ?: ScanResult(NONE)
    }

    private fun skalIkkeScanne(bytes: ByteArray, cf: VirusScanConfig) = bytes.isEmpty() || !cf.isEnabled

    companion object {
        private val PDF = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D)
    }
}

class AttachmentException(msg: String?) : RuntimeException(msg)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScanResult(val result: Result) {

    enum class Result {
        FOUND,
        OK,
        NONE
    }

    companion object {
        val FEIL = listOf(ScanResult(NONE))
    }
}