package no.nav.aap.api.søknad.arkiv.pdf

/*@Component
class PDFA1BSjekker : PDFSjekker() {

    override fun doSjekk(dokument: DokumentInfo) =
        with(dokument) {
            PreflightParser(ByteArrayDataSource(ByteArrayInputStream(bytes))).apply {
                parse(PDF_A1B)
                preflightDocument.use {
                    it.validate()
                    if (it.result.isValid) {
                        log.trace(CONFIDENTIAL, "PDF-A1B validering resultat OK for $filnavn")
                    }
                    else {
                        log.trace(CONFIDENTIAL, "PDF-A1B validering feilet for $filnavn")
                    }
                }
            }
            Unit
        }
}
*/