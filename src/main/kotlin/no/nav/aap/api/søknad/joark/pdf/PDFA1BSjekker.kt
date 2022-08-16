package no.nav.aap.api.søknad.joark.pdf

import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.apache.pdfbox.preflight.Format.PDF_A1B
import org.apache.pdfbox.preflight.parser.PreflightParser
import org.apache.pdfbox.preflight.utils.ByteArrayDataSource
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class PDFA1BSjekker : DokumentSjekker {
    private val log = getLogger(javaClass)

    override fun sjekk(dokument: DokumentInfo) =

        with(dokument) {
            if (APPLICATION_PDF_VALUE == contentType) {
                PreflightParser(ByteArrayDataSource(ByteArrayInputStream(bytes))).apply {
                    parse(PDF_A1B)
                    preflightDocument.use {
                        it.validate()
                        if (it.result.isValid) {
                            log.info(CONFIDENTIAL, "PDF-A1B validering resultat OK for $filnavn")
                        }
                        else {
                            log.trace(CONFIDENTIAL, "PDF-A1B validering feilet for $filnavn")
                        }
                    }
                }
            }
            else {
                log.trace("Ingen PDF-A1B validering av $contentType")
            }
            Unit
        }
}