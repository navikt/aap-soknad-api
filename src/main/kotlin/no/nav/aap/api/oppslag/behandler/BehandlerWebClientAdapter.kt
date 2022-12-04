package no.nav.aap.api.oppslag.behandler

import no.nav.aap.api.oppslag.behandler.BehandlerConfig.Companion.BEHANDLER
import no.nav.aap.api.oppslag.behandler.BehandlerConfig.Companion.BEHANDLERPING
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class BehandlerWebClientAdapter(
        @Qualifier(BEHANDLER) webClient: WebClient,
        @Qualifier(BEHANDLERPING) pingClient: WebClient,
        val cf: BehandlerConfig) : AbstractWebClientAdapter(webClient, cf,pingClient) {

    fun behandlerInfo() = webClient
        .get()
        .uri(cf::path)
        .accept(APPLICATION_JSON)
        .retrieve()
        .onStatus({ NOT_FOUND == it }, { Mono.empty<Throwable?>().also { log.trace("Behandler ikke funnet") } })
        .bodyToMono<List<BehandlerDTO>>()
        .retryWhen(cf.retrySpec(log))
        .onErrorResume { Mono.empty<List<BehandlerDTO>>().also { log.warn("Behandler oppslag feilet", it) } }
        .block()
        ?.map { it.tilBehandler() }
        .orEmpty()
        .also { log.trace(CONFIDENTIAL,"Behandlere mappet er $it") }

    override fun toString() = "${javaClass.simpleName} [webClient=$webClient, cfg=$cfg]"
}