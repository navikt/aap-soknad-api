package no.nav.aap.api.søknad.arkiv.pdf

import org.apache.pdfbox.pdmodel.PDDocument.load
import org.springframework.stereotype.Component
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo

@Component
class PDFLastbarhetSjekker : PDFSjekker() {
    override fun doSjekk(dokument: DokumentInfo) = load(dokument.bytes).use { }
}