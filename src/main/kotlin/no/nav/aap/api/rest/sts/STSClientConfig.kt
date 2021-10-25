package no.nav.aap.api.rest.sts

import no.nav.aap.api.config.Constants.STS
import no.nav.aap.api.rest.AbstractRestConfig.Companion.correlatingFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient

@Configuration
internal class STSClientConfig {
    @Bean
    @Qualifier(STS)
    fun webClientSTS(builder: WebClient.Builder, cfg: STSConfig): WebClient {
        return builder
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction())
            .defaultHeaders { h: HttpHeaders -> h.setBasicAuth(cfg.username, cfg.password) }
            .build()
    }
}