package no.nav.aap.api.søknad.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.søknad.tokenx.TokenXFilterFunction
import no.nav.aap.api.søknad.util.MDCUtil
import no.nav.aap.api.søknad.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.api.søknad.util.MDCUtil.NAV_CALL_ID1
import no.nav.aap.api.søknad.util.MDCUtil.NAV_CONSUMER_ID
import no.nav.aap.api.søknad.util.MDCUtil.callId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

@Configuration
 class PDLClientConfig {
    @Value("aap-soknad-api")
    private val consumer: String = ""
    private fun consumerId(): String? {
        return Optional.ofNullable(MDCUtil.consumerId())
            .orElse(consumer)
    }

    private fun correlatingFilterFunction(): ExchangeFilterFunction {
        return ExchangeFilterFunction { req: ClientRequest, next: ExchangeFunction ->
            next.exchange(
                ClientRequest.from(req)
                    .header(NAV_CONSUMER_ID, consumerId())
                    .header(NAV_CALL_ID, callId())
                    .header(NAV_CALL_ID1, callId())
                    .build()
            )
        }
    }

    @Qualifier(PDL_USER)
    @Bean
    open fun webClientPDL(
        builder: WebClient.Builder,
        cfg: PDLConfig,
        tokenXFilterFunction: TokenXFilterFunction
    ): WebClient {
        return builder
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction())
            .filter(temaFilterFunction())
            .filter(tokenXFilterFunction)
            .build()
    }

    @Qualifier(PDL_USER)
    @Bean
    open fun pdlWebClient(@Qualifier(PDL_USER) client: WebClient?, mapper: ObjectMapper?): GraphQLWebClient {
        return GraphQLWebClient.newInstance(client, mapper)
    }

    companion object {
        const val PDL_USER = "PDL"
        const val STS = "STS"
        private const val TEMA = "TEMA"
        private val LOG = LoggerFactory.getLogger(PDLClientConfig::class.java)
        private const val AAP = "AAP"
        private const val NAV_CONSUMER_TOKEN = "Nav-Consumer-Token"
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
}