package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdConfig.Companion.ARBEIDSFORHOLD
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate.now


@Component
class ArbeidsforholdClientAdapter(
        @Qualifier(ARBEIDSFORHOLD) webClient: WebClient,
        private val cf: ArbeidsforholdConfig) : AbstractWebClientAdapter(webClient, cf) {


    fun arbeidsforhold() =
         webClient
            .get()
             .uri { b -> cf.arbeidsforholdURI(b, now().minus(cf.tidTilbake)) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatus::isError, ClientResponse::createException)
            .toEntityList(ArbeidsforholdDTO::class.java)
            .block()
            ?.body
             ?.map { it.tilArbeidsforhold() }.orEmpty()


    override fun toString() = "${javaClass.simpleName} [webClient=$webClient, cfg=$cf]"
}