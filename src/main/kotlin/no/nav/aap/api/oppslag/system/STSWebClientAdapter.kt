package no.nav.aap.api.oppslag.system

import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.Constants.STS
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
public class STSWebClientAdapter(@Qualifier(STS) val  client: WebClient, private val cf: STSConfig) : AbstractWebClientAdapter(client,cf) {

    val slack = cf.slack
    override fun pingEndpoint() = cfg.pingEndpoint

    fun refresh() : SystemToken? =
        webClient
            .post()
            .uri(cf::stsURI)
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_FORM_URLENCODED)
            .body(cf.stsBody())
            .retrieve()
            .onStatus({ obj: HttpStatus -> obj.isError }) { obj: ClientResponse -> obj.createException() }
            .bodyToMono<SystemToken>()
            .block()
    }