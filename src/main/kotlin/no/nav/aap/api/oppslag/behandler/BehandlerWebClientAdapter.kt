package no.nav.aap.api.oppslag.behandler

import no.nav.aap.api.oppslag.behandler.BehandlerConfig.Companion.BEHANDLER
import no.nav.aap.api.oppslag.behandler.BehandlerConfig.Companion.BEHANDLERPING
import no.nav.aap.rest.AbstractRetryingWebClientAdapter
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class BehandlerWebClientAdapter(
        @Qualifier(BEHANDLER) webClient: WebClient,
        @Qualifier(BEHANDLERPING) pingClient: WebClient,
        val cf: BehandlerConfig) : AbstractRetryingWebClientAdapter(webClient, cf,pingClient) {

    fun behandlerInfo() = webClient
        .get()
        .uri(cf::path)
        .accept(APPLICATION_JSON)
        .retrieve()
        .bodyToMono<List<BehandlerDTO>>()
        .doOnError { t: Throwable ->
            log.warn("Behandler oppslag feilet", t)
        }
        .onErrorReturn(listOf())
        .doOnSuccess {
            log.trace(CONFIDENTIAL,"Behandlere er $it")
        }
        .block()
        ?.map {
            it.tilBehandler()
        }
        .orEmpty()
        .also {
            log.trace(CONFIDENTIAL,"Behandlere mappet er $it")
        }

    override fun toString() = "${javaClass.simpleName} [webClient=$webClient, cfg=$cfg]"
}