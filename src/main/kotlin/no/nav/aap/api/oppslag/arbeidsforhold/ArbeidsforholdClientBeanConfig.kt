package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdConfig.Companion.ARBEIDSFORHOLD
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder


@Configuration
class ArbeidsforholdClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String) {

    @Bean
    @Qualifier(ARBEIDSFORHOLD)
    fun webClientArbeidsforholdTokenX(builder: Builder,
                                      cfg: ArbeidsforholdConfig,
                                      tokenXFilterFunction: TokenXFilterFunction): WebClient? {
        return builder
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction(applicationName))
            .filter(tokenXFilterFunction)
            .build()
    }
}