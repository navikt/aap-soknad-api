package no.nav.aap.api.oppslag.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.config.Constants.PDL_USER
import no.nav.aap.api.rest.AbstractRestConfig.Companion.correlatingFilterFunction
import no.nav.aap.api.rest.AbstractRestConfig.Companion.temaFilterFunction
import no.nav.aap.api.rest.tokenx.TokenXFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
 class PDLClientBeanConfig()  {
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


}