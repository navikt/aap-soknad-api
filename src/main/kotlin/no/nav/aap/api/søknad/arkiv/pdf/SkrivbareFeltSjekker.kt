package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker
import no.nav.aap.util.LoggerUtil.getLogger
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class SkrivbareFeltSjekker : DokumentSjekker {
    private val log = getLogger(javaClass)

    override fun sjekk(dokument: DokumentInfo) =
        with(dokument) {
            if (APPLICATION_PDF_VALUE == contentType) {
                try {
                    ByteArrayInputStream(bytes).use { inputStream ->
                        PDDocument.load(inputStream).use { pdfDocument ->
                            pdfDocument.documentCatalog.acroForm?.let {
                                log.warn("Fant skrivbare felter i $filnavn")
                            } ?: log.info("Ingen skrivbare felter i $filnavn")
                        }
                    }
                } catch (e: Exception) {
                   log.warn("Feil ved sjekking av skrivbare felti $filnavn",e)
                }
            }
            else {
                log.trace("Ingen skrivbare-felt validering av $filnavn med type $contentType")
            }
            Unit
        }
}