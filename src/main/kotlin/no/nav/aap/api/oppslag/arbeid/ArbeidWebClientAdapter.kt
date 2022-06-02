package no.nav.aap.api.oppslag.arbeid

import no.nav.aap.api.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

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
            .bodyToMono<List<ArbeidsforholdDTO>>()
            .doOnError { t: Throwable -> log.warn("Arbeidsforhold oppslag feilet", t) }
            .doOnSuccess { log.trace("Arbeidsforhold er $it") }
            .block() ?: listOf()

    override fun toString() = "${javaClass.simpleName} [webClient=$webClient, cfg=$cf]"
}