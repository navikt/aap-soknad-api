package no.nav.aap.api.oppslag.konto

import no.nav.aap.api.felles.Kontonummer
import no.nav.aap.api.oppslag.konto.KontoConfig.Companion.KONTO
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.AuthContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class KontoWebClientAdapter(@Qualifier(KONTO) client: WebClient,
                            private val ctx: AuthContext,
                            private val cf: KontoConfig) :
    AbstractWebClientAdapter(client, cf) {

    fun kontoInformasjon(historikk: Boolean = false) =
        if (cf.isEnabled) {
            webClient.get()
                .uri(cf::kontoUri)
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus({ NOT_FOUND == it }, { Mono.empty() })
                .bodyToMono<Map<String, String>>()
                .doOnSuccess {
                    log.trace("Kontoinformasjon er $it")
                }
                .doOnError { t: Throwable ->
                    log.warn("Kontoinformasjon oppslag feilet", t)
                }

                .defaultIfEmpty(emptyMap())
                .onErrorReturn(emptyMap())
                .block()?.tilKontonummer()
        }
        else null

    private fun Map<String, String>.tilKontonummer() = this["kontonummer"]?.let { Kontonummer(it) }
}