package no.nav.aap.api.rest

import no.nav.aap.api.config.Constants
import no.nav.aap.api.util.MDCUtil
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.web.reactive.function.client.*
import java.net.URI

abstract class AbstractWebClientAdapter(protected val webClient: WebClient, protected val cfg: AbstractRestConfig) :
    RetryAware, Pingable {

    override fun name() = cfg.name()
    protected val baseUri: URI = cfg.baseUri
    override fun pingEndpoint() = cfg.pingEndpoint()

    override fun ping() {
        webClient
            .get()
            .uri(pingEndpoint())
            .accept(APPLICATION_JSON, TEXT_PLAIN)
            .retrieve()
            .onStatus({ obj: HttpStatus -> obj.isError }) { obj: ClientResponse -> obj.createException() }
            .toBodilessEntity()
            .block()
    }

    companion object {
        fun correlatingFilterFunction() =
            ExchangeFilterFunction { req: ClientRequest, next: ExchangeFunction ->
                next.exchange(
                    ClientRequest.from(req)
                        .header(MDCUtil.NAV_CONSUMER_ID, MDCUtil.consumerId())
                        .header(MDCUtil.NAV_CALL_ID, MDCUtil.callId())
                        .header(MDCUtil.NAV_CALL_ID1, MDCUtil.callId())
                        .build()
                )
            }

        fun temaFilterFunction() =
            ExchangeFilterFunction { req: ClientRequest, next: ExchangeFunction ->
                next.exchange(
                    ClientRequest.from(req)
                        .header(Constants.TEMA, Constants.AAP)
                        .build()
                )
            }
    }

    override fun toString() = "${javaClass.simpleName} [webClient=$webClient,graphQLWebClient=$cfg]"
}