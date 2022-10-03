package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.DokumentException.Substatus.PASSWORD_PROTECTED
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.apache.commons.lang3.exception.ExceptionUtils.hasCause
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE

abstract class AbstractPDFSjekker : DokumentSjekker {
    protected val log = getLogger(javaClass)

    abstract fun doSjekk(dokument: DokumentInfo)

    override fun sjekk(dokument: DokumentInfo) {
        if (APPLICATION_PDF_VALUE == dokument.contentType) {
            runCatching {
                log.trace("${javaClass.simpleName} sjekker ${dokument.filnavn}")
                doSjekk(dokument)
            }.getOrElse {
                when (it) {
                    is InvalidPasswordException -> throw PassordBeskyttetException(" ${dokument.filnavn} er passord-beskyttet, og vil ikke kunne leses av en saksbehandler, fjern beskyttelsen og prøv igjen",
                            it)

                    is Exception ->
                        if (hasCause(it, InvalidPasswordException::class.java)) {
                            throw PassordBeskyttetException(" ${dokument.filnavn} er passord-beskyttet, og vil ikke kunne leses av en saksbehandler, fjern beskyttelsen og prøv igjen",
                                    it)
                        }

                    else -> throw DokumentException("Uventet feil ved sjekk av ${dokument.filnavn}", it)
                }
            }
        }
        else {
            log.trace(CONFIDENTIAL, "Sjekker ikke ${dokument.contentType}")
        }
    }

    class PassordBeskyttetException(msg: String, cause: Exception) : DokumentException(msg, cause, PASSWORD_PROTECTED)
}