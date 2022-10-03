package no.nav.aap.api.søknad.arkiv.pdf

import com.fasterxml.jackson.databind.ObjectMapper
import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.arkiv.pdf.PDFGeneratorConfig.Companion.PDF
import no.nav.aap.api.søknad.model.PDFKvittering
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.StringExtensions.toJson
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.LocalDate.now

@Component
class PDFGeneratorWebClientAdapter(@Qualifier(PDF) client: WebClient,
                                   private val cf: PDFGeneratorConfig,
                                   private val mapper: ObjectMapper) : AbstractWebClientAdapter(client, cf) {
    fun generate(søker: Søker, kvittering: PDFKvittering) = generate(cf.standardPath, StandardData(søker, kvittering).toJson(mapper))
    fun generate(søker: Søker, søknad: UtlandSøknad) = generate(cf.utlandPath, UtlandData(søker, søknad).toJson(mapper))
    private fun generate(path: String, data: Any) =
        webClient.post()
            .uri { it.path(path).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(data)
            .retrieve()
            .bodyToMono<ByteArray>()
            .doOnError { t: Throwable ->
                log.warn("PDF-generering mot $path feiler", t)
            }
            .doOnSuccess {
                log.trace(CONFIDENTIAL, "Sendte json $data")
                log.info("PDF-generering OK")
            }
            .block() ?: throw IntegrationException("O bytes i retur fra pdfgen, pussig")

    private data class StandardData(val søker: Søker, val kvittering: PDFKvittering)
    private data class UtlandData constructor(val fødselsnummer: Fødselsnummer,
                                              val landKode: CountryCode,
                                              val land: String,
                                              val navn: Navn?,
                                              val periode: Periode,
                                              val dato: LocalDate = now()) {
        constructor(søker: Søker, søknad: UtlandSøknad) : this(søker.fnr,
                søknad.land,
                søknad.land.toLocale().displayName,
                søker.navn,
                søknad.periode)
    }
}