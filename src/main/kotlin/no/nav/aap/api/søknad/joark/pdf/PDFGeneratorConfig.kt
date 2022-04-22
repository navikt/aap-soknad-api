package no.nav.aap.api.søknad.joark.pdf

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConfigurationProperties(prefix = "pdf")
@ConstructorBinding
class PDFGeneratorConfig(
        @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
        @DefaultValue(DEFAULT_PATH) val path: String,
        @DefaultValue(STANDARD_PATH) val standardPath: String,
        @DefaultValue("true") enabled: Boolean,
        @DefaultValue(DEFAULT_BASE_URI) baseUri: URI) : AbstractRestConfig(baseUri, pingPath, enabled) {
    companion object {
        const val PDFGEN = "PDFGEN"
        private const val DEFAULT_BASE_URI = "http://pdfgen"
        private const val DEFAULT_PATH = "api/v1/genpdf/aap-pdfgen/soknad-utland"
        private const val STANDARD_PATH = "api/v1/genpdf/aap-pdfgen/soknad-standard"
        private const val DEFAULT_PING_PATH = "/"
    }
}