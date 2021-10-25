package no.nav.aap.api.rest.sts

import no.nav.aap.api.config.Constants.STS
import no.nav.aap.api.rest.AbstractWebClientAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration


@Component
class STSWebClientAdapter(@Qualifier(STS) webClient: WebClient, override val cfg: STSConfig) :  AbstractWebClientAdapter(webClient, cfg) {
    private val LOG: Logger = LoggerFactory.getLogger(STSWebClientAdapter::class.java)

    fun refresh(): SystemToken? {
        LOG.trace("Refresh av system token")
        val token  = webClient
            .post()
            .uri(cfg::stsURI)
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_FORM_URLENCODED)
            .body(cfg.body())
            .exchange()
            .block()
            ?.bodyToMono<SystemToken>(SystemToken::class.java)
            ?.block()
        LOG.trace("Refresh av system token OK ({})", token?.expiration)
        return token
    }

    val slack: Duration = cfg.slack
    val name =  "STS"

    override fun toString() = "${javaClass.simpleName} [[cfg=$cfg]"
}