package no.nav.aap.api.oppslag.arbeid

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.*
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import no.nav.aap.api.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import no.nav.aap.rest.AbstractWebClientAdapter

@Component
class ArbeidWebClientAdapter(
    @Qualifier(ARBEID) webClient : WebClient,
    private val cf : ArbeidConfig) : AbstractWebClientAdapter(webClient, cf) {

    fun arbeidInfo() =
        if (cf.isEnabled) {
            webClient
                .get()
                .uri(cf::arbeidsforholdURI)
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono<List<ArbeidsforholdDTO>>()
                .retryWhen(cf.retrySpec(log))
                .onErrorResume { Mono.empty() }
                .doOnError { t -> log.warn("Arbeidsforhold oppslag feilet", t) }
                .doOnSuccess { log.trace("Arbeidsforhold er {}", it) }
                .defaultIfEmpty(listOf())
                .contextCapture()
                .block().orEmpty()
        }
        else {
            listOf()
        }

    override fun toString() = "${javaClass.simpleName} [webClient=$webClient, cfg=$cf]"
}