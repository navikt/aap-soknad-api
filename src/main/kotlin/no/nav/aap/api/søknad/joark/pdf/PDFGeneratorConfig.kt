package no.nav.aap.api.søknad.joark.pdf

import no.nav.aap.api.søknad.joark.pdf.PDFGeneratorConfig.Companion.PDF
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.util.unit.DataSize
import java.net.URI

@ConfigurationProperties(PDF)
@ConstructorBinding
class PDFGeneratorConfig(
        @DefaultValue("50Mb") val codecSize: DataSize,
        @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
        @DefaultValue(UTLAND_PATH) val utlandPath: String,
        @DefaultValue(STANDARD_PATH) val standardPath: String,
        @DefaultValue("true") enabled: Boolean,
        @DefaultValue(DEFAULT_BASE_URI) baseUri: URI) : AbstractRestConfig(baseUri, pingPath, PDF, enabled) {
    companion object {
        const val PDF = "pdf"
        private const val DEFAULT_BASE_URI = "http://pdfgen"
        private const val UTLAND_PATH = "api/v1/genpdf/aap-pdfgen/soknad-utland"
        private const val STANDARD_PATH = "api/v1/genpdf/aap-pdfgen/soknad-standard"
        private const val DEFAULT_PING_PATH = "/"
    }
}