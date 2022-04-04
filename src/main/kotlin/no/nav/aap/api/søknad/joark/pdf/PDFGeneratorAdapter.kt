package no.nav.aap.api.søknad.joark.pdf

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.ObjectMapper
import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.felles.UtenlandsSøknadKafka
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.api.søknad.joark.pdf.PDFGeneratorConfig.Companion.PDFGEN
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.LocalDate.now

@Component
class PDFGeneratorAdapter(@Qualifier(PDFGEN) client: WebClient, val cf: PDFGeneratorConfig, val mapper: ObjectMapper) :
    AbstractWebClientAdapter(client, cf) {

    fun generate(søknad: UtenlandsSøknadKafka) =
        webClient.post()
            .uri { it.path(cf.path).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(søknad.pdfData(mapper))
            .retrieve()
            .onStatus({ obj: HttpStatus -> obj.isError }) { obj: ClientResponse -> obj.createException() }
            .bodyToMono<ByteArray>()
            .block()
}

private fun UtenlandsSøknadKafka.pdfData(m: ObjectMapper) =
    m.writeValueAsString(PDFData(søker.fnr, land.land(), søker.navn, periode))

private fun CountryCode.land() = toLocale().displayCountry

private data class PDFData(val fødselsnummer: Fødselsnummer,
                           val land: String, @get:JsonUnwrapped val navn: Navn?,
                           @get:JsonUnwrapped val periode: Periode,
                           @get:JsonFormat(
                                   shape = STRING,
                                   pattern = "dd.MM.yyyy") val dato: LocalDate = now())