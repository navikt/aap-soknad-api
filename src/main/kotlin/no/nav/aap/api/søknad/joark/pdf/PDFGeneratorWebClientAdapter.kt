package no.nav.aap.api.søknad.joark.pdf

import com.fasterxml.jackson.databind.ObjectMapper
import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.joark.pdf.PDFGeneratorConfig.Companion.PDFGEN
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.LocalDate.now

@Component
class PDFGeneratorWebClientAdapter(@Qualifier(PDFGEN) client: WebClient, private val cf: PDFGeneratorConfig, private val mapper: ObjectMapper) : AbstractWebClientAdapter(client, cf) {
    fun generate(søker: Søker, søknad: StandardSøknad) = generate(StandardData(søker, søknad))
    fun generate(søker: Søker, søknad: UtlandSøknad) = generate(UtlandData(søker, søknad))
    private fun generate(data: Any) =
        webClient.post()
            .uri { it.path(cf.standardPath).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(mapper.writeValueAsString(data))
            .retrieve()
            .bodyToMono<ByteArray>()
            .doOnError { t: Throwable -> log.warn("PDF-generering feiler", t) }
            .doOnSuccess { log.trace("PDF-generering OK") }
            .block() ?: throw IntegrationException("O bytes i retur fra pdfgen, pussig")
     data class StandardData(val søker: Søker, val søknad: StandardSøknad)

 data class UtlandData  constructor(val fødselsnummer: Fødselsnummer, val landKode: CountryCode, val land: String, val navn: Navn?, val periode: Periode, val dato: LocalDate = now()) {
    constructor(søker: Søker, søknad: UtlandSøknad) : this(søker.fødselsnummer,søknad.land,søknad.land.toLocale().displayName,søker.navn,søknad.periode)
 }
}