package no.nav.aap.api.oppslag.behandler

import no.nav.aap.api.oppslag.behandler.BehandlerConfig.Companion.BEHANDLER
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.scheduler.Schedulers


@Component
class BehandlerWebClientAdapter(
        @Qualifier(BEHANDLER) webClient: WebClient,
         val cf: BehandlerConfig) : AbstractWebClientAdapter(webClient, cf) {

    fun behandlere() = webClient
        .get()
        .uri(cf::path)
        .accept(APPLICATION_JSON)
        .retrieve()
        .bodyToFlux(BehandlerDTO::class.java)
        .doOnError { t: Throwable -> log.warn("Behandler oppslag feilet", t) }
        .collectList()
        .doOnSuccess {  log.trace("Behandlere er $it")}
        .block()
        ?.map { it.tilBehandler() }
        .orEmpty()
        .also { log.trace("Behandlere mappet er $it") }

    override fun toString() = "${javaClass.simpleName} [webClient=$webClient, cfg=$cfg]"
}