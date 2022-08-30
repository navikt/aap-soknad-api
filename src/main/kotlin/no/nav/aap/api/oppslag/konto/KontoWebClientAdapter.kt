package no.nav.aap.api.oppslag.konto

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.oppslag.konto.KontoConfig.Companion.KONTO
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.AuthContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KontoWebClientAdapter(@Qualifier(KONTO) client: WebClient,
                            private val ctx: AuthContext,
                            private val cf: KontoConfig) :
    AbstractWebClientAdapter(client, cf) {

    fun kontoInformasjon(historikk: Boolean = false) =
        webClient.post()
            .uri(cf::kontoUri)
            .bodyValue(Body(ctx.getFnr(), historikk))
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<Map<String, String>>()
            .doOnSuccess {
                log.trace("Kontoinformasjon er $it")
            }
            .onErrorReturn(mapOf())
            // .doOnError { t: Throwable ->
            //     log.warn("Kontoinformasjon oppslag feilet", t)
            //  }
            .block()

    internal data class Body(val kontohaver: Fødselsnummer, val historikk: Boolean)
}