package no.nav.aap.api.rest

import no.nav.aap.api.config.Constants
import no.nav.aap.api.util.MDCUtil
import no.nav.aap.api.util.URIUtil
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import java.net.URI

 abstract class AbstractRestConfig  protected constructor(val baseUri: URI, protected val pingPath: String, val isEnabled: Boolean) {
    fun pingEndpoint() = URIUtil.uri(baseUri, pingPath)
    fun name(): String = baseUri.host

     companion object {
      fun correlatingFilterFunction(): ExchangeFilterFunction {
         return ExchangeFilterFunction { req: ClientRequest, next: ExchangeFunction ->
             next.exchange(
                 ClientRequest.from(req)
                     .header(MDCUtil.NAV_CONSUMER_ID, MDCUtil.consumerId())
                     .header(MDCUtil.NAV_CALL_ID, MDCUtil.callId())
                     .header(MDCUtil.NAV_CALL_ID1, MDCUtil.callId())
                     .build()
             )
         }
     }
      fun temaFilterFunction(): ExchangeFilterFunction {
         return ExchangeFilterFunction { req: ClientRequest, next: ExchangeFunction ->
             next.exchange(
                 ClientRequest.from(req)
                     .header(Constants.TEMA, Constants.AAP)
                     .build()
             )
         }
     }
     }
}