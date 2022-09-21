package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker
import no.nav.aap.util.LoggerUtil.getLogger
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class EditerbareFeltSjekker : DokumentSjekker {
    private val log = getLogger(javaClass)

    override fun sjekk(dokument: DokumentInfo) =
        with(dokument) {
            if (APPLICATION_PDF_VALUE == contentType) {
                runCatching {
                    log.trace("Sjekker $filnavn for skrivbare felter")
                    ByteArrayInputStream(bytes).use { inputStream ->
                        PDDocument.load(inputStream).use {
                            it.documentCatalog.acroForm?.let {
                                log.warn("Fant editerbare felter i $filnavn")
                            } ?: log.info("Ingen editerbare felter i $filnavn")
                        }
                    }
                }.getOrElse {log.warn("Feil ved sjekking av editerbare felt i $filnavn",it) }
            }
            else {
                log.trace("Ingen editerbare-felt validering av $filnavn med type $contentType")
            }
        }
}