package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocument.*
import org.springframework.stereotype.Component

@Component
class PDFLastbarhetSjekker : AbstractPDFSjekker() {
    override fun doSjekk(dokument: DokumentInfo) = load(dokument.bytes).use { }
}