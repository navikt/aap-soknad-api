package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE

abstract class AbstractPDFSjekker: DokumentSjekker {
    protected val log = getLogger(javaClass)

    abstract fun doSjekk(dokument: DokumentInfo)

    override fun sjekk(dokument: DokumentInfo) {
        if (APPLICATION_PDF_VALUE == dokument.contentType) {
            runCatching {
                log.trace("Sjekker ${dokument.filnavn}")
                doSjekk(dokument)
            }.getOrElse {log.warn("Feil ved sjekking av  ${dokument.filnavn}") }
        }
        else {
            log.trace(CONFIDENTIAL, "Sjekker ikke ${dokument.contentType}")
        }
    }
}