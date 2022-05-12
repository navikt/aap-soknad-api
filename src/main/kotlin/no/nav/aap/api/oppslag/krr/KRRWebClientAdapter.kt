package no.nav.aap.api.oppslag.krr

import no.nav.aap.api.oppslag.krr.KRRConfig.Companion.KRR
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KRRWebClientAdapter(@Qualifier(KRR) client: WebClient, val cf: KRRConfig) : AbstractWebClientAdapter(client, cf) {

    fun kontaktInformasjon() =
        webClient.get()
            .uri(cf::kontaktUri)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<KontaktinformasjonDTO>()
            .doOnSuccess { log.trace("Kontaktinformasjon er $it") }
            .doOnError { t: Throwable -> log.warn("Krr oppslag feilet", t) }
            .block()
}