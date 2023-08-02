package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.pdmodel.PDDocument.load
import org.springframework.stereotype.Component

@Component
class PDFLastbarhetOgEditerbarSjekker : PDFSjekker() {
    override fun doSjekk(dokument: DokumentInfo) =
        load(dokument.bytes, "", null, null, MemoryUsageSetting.setupTempFileOnly()).use {
            it.documentCatalog.acroForm?.let {
                log.warn("Fant editerbare felter i ${dokument.filnavn}")
            } ?: log.trace("Ingen editerbare felter i ${dokument.filnavn}")
        }
}