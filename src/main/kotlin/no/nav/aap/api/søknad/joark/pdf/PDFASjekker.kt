package no.nav.aap.api.søknad.joark.pdf

import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker
import no.nav.aap.util.LoggerUtil.getLogger
import org.apache.pdfbox.preflight.parser.PreflightParser
import org.apache.pdfbox.preflight.utils.ByteArrayDataSource
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class PDFASjekker : DokumentSjekker {
    private val log = getLogger(javaClass)

    override fun sjekk(dokument: DokumentInfo) = try {
        val parser = PreflightParser(ByteArrayDataSource(ByteArrayInputStream(dokument.bytes)))
        parser.parse()
        parser.preflightDocument.use {
            it.validate()
            with(it.result) {
                if (isValid) {
                    log.info("PDF/A validering resultat OK")
                }
                else {
                    log.trace("PDF/A validering feilet")
                }
            }
        }
    }
    catch (e: Exception) {
        if (e is DokumentException) throw e
        else log.warn("Uventet feil ved sjekking av $dokument", e)
    }
}