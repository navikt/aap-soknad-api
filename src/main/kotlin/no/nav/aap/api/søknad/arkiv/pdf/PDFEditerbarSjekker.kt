package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class PDFEditerbarSjekker : AbstractPDFSjekker() {
    override fun doSjekk(dokument: DokumentInfo) =
        with(dokument) {
            ByteArrayInputStream(bytes).use { inputStream ->
                PDDocument.load(inputStream).use {
                    it.documentCatalog.acroForm?.let {
                        log.warn("Fant editerbare felter i $filnavn")
                    } ?: log.info("Ingen editerbare felter i $filnavn")
                }
            }
        }
}