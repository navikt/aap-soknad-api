package no.nav.aap.api.oppslag.kontaktinformasjon

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import no.nav.aap.api.oppslag.kontaktinformasjon.KRRConfig.Companion.KRR
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL

@Component
class KRRWebClientAdapter(@Qualifier(KRR) client: WebClient, val cf: KRRConfig) : AbstractWebClientAdapter(client, cf) {

    fun kontaktInformasjon() =
        webClient.get()
            .uri(cf::kontaktUri)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<KontaktinformasjonDTO>()
            .retryWhen(cf.retrySpec(log))
            .doOnSuccess { log.trace(CONFIDENTIAL, "Kontaktinformasjon fra KRR er {}", it) }
            .doOnError { log.warn("KRR oppslag feilet") }
            .onErrorReturn(KontaktinformasjonDTO())
            .contextCapture()
            .block()?.tilKontaktinfo()
}