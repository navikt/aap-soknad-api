package no.nav.aap.api.oppslag.arbeid

import no.nav.aap.api.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class ArbeidWebClientAdapter(
        @Qualifier(ARBEID) webClient: WebClient,
        private val cf: ArbeidConfig) : AbstractWebClientAdapter(webClient, cf) {

    fun arbeidsforhold() =
        webClient
            .get()
            .uri(cf::arbeidsforholdURI)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(ArbeidsforholdDTO::class.java)
            .doOnError { t: Throwable -> log.warn("Arbeidsforhold oppslag feilet", t) }
            .collectList()
            .doOnSuccess { log.trace("Arbeidsforhold er $it") }
            .block()

    override fun toString() = "${javaClass.simpleName} [webClient=$webClient, cfg=$cf]"
}