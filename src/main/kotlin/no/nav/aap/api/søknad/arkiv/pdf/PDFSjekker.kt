package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.error.Substatus.PASSWORD_PROTECTED
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.apache.commons.lang3.exception.ExceptionUtils.hasCause
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE

abstract class PDFSjekker : DokumentSjekker {
    protected val log = getLogger(javaClass)

    abstract fun doSjekk(dokument: DokumentInfo)

    override fun sjekk(dokument: DokumentInfo) {
        with(dokument) {
            if (APPLICATION_PDF_VALUE == contentType) {
                runCatching {
                    log.trace("${javaClass.simpleName} sjekker $filnavn")
                    doSjekk(dokument)
                }.getOrElse {
                    when (it) {
                        is InvalidPasswordException -> beskyttet(null)
                        is Exception -> muligensBeskyttet(filnavn,it)
                        else ->  uventet(filnavn,it)
                    }
                }
            }
            else {
                log.trace(CONFIDENTIAL, "Sjekker ikke $contentType")
            }
        }
    }

    private fun muligensBeskyttet(filnavn: String?, it: Throwable) : Nothing =
        if (hasCause(it, InvalidPasswordException::class.java)) {
            beskyttet(it)
        }
        else {
            uventet(filnavn,it)
        }
    private fun uventet(filnavn: String?, cause: Throwable) : Nothing = throw DokumentException("Uventet feil ved sjekk av $filnavn", cause)

    private fun beskyttet(cause: Throwable?) : Nothing =
        throw PassordBeskyttetException("filnavn er passord-beskyttet, og kan ikke leses av en saksbehandler, fjern beskyttelsen og prøv igjen", cause)

    class PassordBeskyttetException(msg: String, cause: Throwable?) : DokumentException(msg, cause, PASSWORD_PROTECTED)
}