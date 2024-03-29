package no.nav.aap.api.søknad.arkiv.pdf

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.søknad.arkiv.pdf.PDFGeneratorConfig.Companion.PDF
import no.nav.aap.health.AbstractPingableHealthIndicator

@Configuration
class PDFGeneratorClientBeanConfig {
    @Qualifier(PDF)
    @Bean
    fun webClientPdfGen(builder: WebClient.Builder, cfg: PDFGeneratorConfig) =
        builder
            .codecs { it.defaultCodecs().maxInMemorySize(cfg.codecSize.toBytes().toInt()) }
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    @ConditionalOnProperty("${PDF}.enabled", havingValue = "true")
    fun pdfGenHealthIndicator(adapter: PDFGeneratorWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}