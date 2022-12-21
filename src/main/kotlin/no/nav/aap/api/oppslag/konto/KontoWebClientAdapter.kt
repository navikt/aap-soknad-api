package no.nav.aap.api.oppslag.konto

import no.nav.aap.api.felles.Kontonummer
import no.nav.aap.api.oppslag.konto.KontoConfig.Companion.KONTO
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class KontoWebClientAdapter(@Qualifier(KONTO) client: WebClient,
                            private val cf: KontoConfig) : AbstractWebClientAdapter(client, cf) {

    fun kontoInfo(historikk: Boolean = false) =
        if (cf.isEnabled) {
            webClient.get()
                .uri(cf::kontoUri)
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus({ NOT_FOUND == it }, { Mono.empty<Throwable>().also {log.trace("Kontoinformasjon ikke funnet") } })
                .bodyToMono<Map<String, Any>>()
                .retryWhen(cf.retrySpec(log))
                .doOnSuccess { log.trace("Kontoinformasjon returnerte  $it") }
                .onErrorResume { Mono.empty() }
                .defaultIfEmpty(emptyMap())
                .block()?.tilKontonummer()
        }
        else null

    private fun Map<String, Any>.tilKontonummer() = this["kontonummer"]?.let{
        it as String }?.let { if (it.length == 11)  Kontonummer(it)  else null }
}