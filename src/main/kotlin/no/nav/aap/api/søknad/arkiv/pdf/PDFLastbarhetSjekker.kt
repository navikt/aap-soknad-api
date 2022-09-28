package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.stereotype.Component

@Component
class PDFPassordSjekker : AbstractPDFSjekker() {
    override fun doSjekk(dokument: DokumentInfo) =
        PDDocument.load(dokument.bytes).use { }
}