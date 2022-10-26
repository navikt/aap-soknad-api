package no.nav.aap.api.søknad.arkiv.pdf

import java.io.ByteArrayInputStream
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import org.apache.pdfbox.pdmodel.PDDocument.load
import org.springframework.stereotype.Component

@Component
class PDFEditerbarSjekker : PDFSjekker() {
    override fun doSjekk(dokument: DokumentInfo) =
        with(dokument) {
            ByteArrayInputStream(bytes).use { inputStream ->
                load(inputStream).use {
                    it.documentCatalog.acroForm?.let {
                        log.warn("Fant editerbare felter i $filnavn")
                    } ?: log.trace("Ingen editerbare felter i $filnavn")
                }
            }
        }
}