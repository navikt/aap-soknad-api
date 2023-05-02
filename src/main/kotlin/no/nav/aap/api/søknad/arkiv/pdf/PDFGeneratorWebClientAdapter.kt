package no.nav.aap.api.søknad.arkiv.pdf

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.api.søknad.arkiv.pdf.PDFGeneratorConfig.Companion.PDF
import no.nav.aap.api.oppslag.person.Søker
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.StringExtensions.toJson
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL

@Component
class PDFGeneratorWebClientAdapter(@Qualifier(PDF) client : WebClient,
                                   private val cf : PDFGeneratorConfig,
                                   private val mapper : ObjectMapper) : AbstractWebClientAdapter(client, cf) {

    fun generate(søker : Søker, kvittering : PDFKvittering) = generate(cf.standardPath, StandardData(søker, kvittering).toJson(mapper))
    private fun generate(path : String, data : Any) =
        webClient.post()
            .uri { it.path(path).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(data)
            .retrieve()
            .bodyToMono<ByteArray>()
            .doOnError { t : Throwable ->
                log.warn("PDF-generering mot $path feiler", t)
            }
            .doOnSuccess {
                log.trace(CONFIDENTIAL, "Sendte JSON {}", data)
            }
            .contextCapture()
            .block() ?: throw IrrecoverableIntegrationException("O bytes i retur fra pdfgen, pussig")

    private data class StandardData(val søker : Søker, val kvittering : PDFKvittering)
}