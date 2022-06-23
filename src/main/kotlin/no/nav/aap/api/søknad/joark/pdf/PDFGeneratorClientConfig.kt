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
            .codecs { it.defaultCodecs().maxInMemorySize(cfg.codecSize.toBytes() as Int) }
            .baseUrl("${cfg.baseUri}")
            .build()
}