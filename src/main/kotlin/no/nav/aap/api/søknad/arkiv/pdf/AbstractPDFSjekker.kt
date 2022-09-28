package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.DokumentException.Substatus.PASSWORD_PROTECTED
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE

abstract class AbstractPDFSjekker: DokumentSjekker {
    protected val log = getLogger(javaClass)

    abstract fun doSjekk(dokument: DokumentInfo)

    override fun sjekk(dokument: DokumentInfo) {
        if (APPLICATION_PDF_VALUE == dokument.contentType) {
            runCatching {
                log.trace("Sjekker ${dokument.filnavn}")
                doSjekk(dokument)
              }.getOrElse {
                log.warn("Feil ved sjekking av ${dokument.filnavn}",it)
                when(it) {
                    is InvalidPasswordException -> throw PassordBeskyttetException(" ${dokument.filnavn} er passord-beskyttet",it)
                    else -> throw it
                }
            }

        }
        else {
            log.trace(CONFIDENTIAL, "Sjekker ikke ${dokument.contentType}")
        }
    }
    class PassordBeskyttetException(msg: String, cause: Exception) : DokumentException(msg, cause, PASSWORD_PROTECTED)
}