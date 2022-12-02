package no.nav.aap.api.søknad.virus

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.micrometer.core.annotation.Timed
import no.nav.aap.api.søknad.virus.ScanResult.Companion.FEIL
import no.nav.aap.api.søknad.virus.ScanResult.Result.FOUND
import no.nav.aap.api.søknad.virus.ScanResult.Result.NONE
import no.nav.aap.api.søknad.virus.ScanResult.Result.OK
import no.nav.aap.api.søknad.virus.VirusScanConfig.Companion.VIRUS
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class VirusScanWebClientAdapter(@Qualifier(VIRUS) client: WebClient, val cf: VirusScanConfig) :
    AbstractWebClientAdapter(client, cf) {
    override fun ping() : Map<String,String> =
        when (harVirus(PDF).result) {
            NONE -> throw VirusException("Uventet ping respons ${NONE.name}")
            FOUND, OK -> emptyMap()
        }

    fun harVirus(bytes: ByteArray) =
        if (skalIkkeScanne(bytes, cf)) {
            log.trace("Ingen scanning av (${bytes.size} bytes, enabled=${cf.enabled})")
            ScanResult(NONE)
        }
        else {
            doScan(bytes)
        }

    @Timed("virus", histogram = true)
    private fun doScan(bytes: ByteArray) =
        webClient
        .put()
        .bodyValue(bytes)
        .accept(APPLICATION_JSON)
        .retrieve()
        .bodyToMono<List<ScanResult>>()
        .doOnError { t: Throwable ->
            log.warn("Virus-respons feilet", t)
        }
        .doOnSuccess {
            log.trace("Virus respons OK")
        }
        .onErrorReturn(FEIL)
        .defaultIfEmpty(FEIL)
        .block()
        ?.single()
        .also {
            log.trace("Fikk scan result $it")
        }
        ?: ScanResult(NONE)

    private fun skalIkkeScanne(bytes: ByteArray, cf: VirusScanConfig) = bytes.isEmpty() || !cf.isEnabled

    companion object {
        private val PDF = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D)
    }
}

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