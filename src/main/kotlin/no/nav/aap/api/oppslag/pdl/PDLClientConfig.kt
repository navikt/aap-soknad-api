package no.nav.aap.api.oppslag.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.config.Constants.AAP
import no.nav.aap.api.config.Constants.PDL_USER
import no.nav.aap.api.config.Constants.TEMA
import no.nav.aap.api.rest.tokenx.TokenXFilterFunction
import no.nav.aap.api.util.MDCUtil
import no.nav.aap.api.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.api.util.MDCUtil.NAV_CALL_ID1
import no.nav.aap.api.util.MDCUtil.NAV_CONSUMER_ID
import no.nav.aap.api.util.MDCUtil.callId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient

@Configuration
 class PDLClientConfig()  {
    @Qualifier(PDL_USER)
    @Bean
     fun webClientPDL(builder: WebClient.Builder, cfg: PDLConfig, tokenXFilterFunction: TokenXFilterFunction): WebClient {
        return builder
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction())
            .filter(temaFilterFunction())
            .filter(tokenXFilterFunction)
            .build()
    }

    @Qualifier(PDL_USER)
    @Bean
     fun pdlWebClient(@Qualifier(PDL_USER) client: WebClient, mapper: ObjectMapper): GraphQLWebClient {
        return GraphQLWebClient.newInstance(client, mapper)
    }

    private fun correlatingFilterFunction(): ExchangeFilterFunction {
        return ExchangeFilterFunction { req: ClientRequest, next: ExchangeFunction ->
            next.exchange(
                ClientRequest.from(req)
                    .header(NAV_CONSUMER_ID, MDCUtil.consumerId())
                    .header(NAV_CALL_ID, callId())
                    .header(NAV_CALL_ID1, callId())
                    .build()
            )
        }
    }

    private fun temaFilterFunction(): ExchangeFilterFunction {
        return ExchangeFilterFunction { req: ClientRequest, next: ExchangeFunction ->
            next.exchange(
                ClientRequest.from(req)
                    .header(TEMA, AAP)
                    .build()
            )
        }
    }
}