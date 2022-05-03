package no.nav.aap.api.søknad.joark.pdf

import no.nav.aap.api.søknad.joark.pdf.PDFGeneratorConfig.Companion.PDF
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient


@Configuration
class PDFGeneratorClientConfig {
    @Qualifier(PDF)
    @Bean
    fun webClientPdfGen(builder: WebClient.Builder, cfg: PDFGeneratorConfig) =
        builder
            .codecs { c -> c.defaultCodecs().maxInMemorySize(50 * 1024 * 1024) }
            .baseUrl("${cfg.baseUri}")
            .build()
}