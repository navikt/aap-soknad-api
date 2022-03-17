package no.nav.aap.api.oppslag.behandler

import no.nav.aap.api.oppslag.behandler.BehandlerConfig.Companion.BEHANDLERE
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient


@Component
class BehandlerWebClientAdapter(
        @Qualifier(BEHANDLERE) webClient: WebClient,
        private val cf: BehandlerConfig) : AbstractWebClientAdapter(webClient, cf) {

    fun behandlere() = webClient
                .get()
                .uri { b -> b.path(cf.path).build() }
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus({ obj: HttpStatus -> obj.isError }) { obj: ClientResponse -> obj.createException() }
                .toEntityList(BehandlerDTO::class.java)
                .block()
                ?.body?.map { it.tilBehandler() }.orEmpty()


    override fun toString() = "${javaClass.simpleName} [webClient=$webClient, cfg=$cf]"
}