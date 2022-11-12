package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.error.Substatus.PASSWORD_PROTECTED
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.apache.commons.lang3.exception.ExceptionUtils.hasCause
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.apache.pdfbox.preflight.exception.ValidationException
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
                }.getOrElse { e ->
                    log.warn("Sjekk av PDF feilet med ${e.javaClass.name}")
                    when (e) {
                        is InvalidPasswordException -> beskyttet(filnavn,e)
                        is ValidationException  -> Unit.also { log.trace("Rar pdf, feiler validering men vi lar den passere")}
                        else  -> muligensBeskyttet(filnavn,e)
                    }
                }
            }
            else {
                log.trace(CONFIDENTIAL, "Sjekker ikke $contentType")
            }
        }
    }

    private fun muligensBeskyttet(filnavn: String?, t: Throwable) : Nothing =
        if (hasCause(t, InvalidPasswordException::class.java)) {
            beskyttet(filnavn,t)
        }
        else {
            uventet(filnavn,t)
        }
    private fun uventet(filnavn: String?, cause: Throwable) : Nothing = throw DokumentException("Uventet feil ved sjekk av $filnavn", cause).also {
        log.warn("Kaster ${it.javaClass.simpleName} med cause ${cause.javaClass.simpleName}")
    }

    private fun beskyttet(filnavn: String?,cause: Throwable?) : Nothing =
        throw PassordBeskyttetException("$filnavn er passord-beskyttet, og kan ikke leses av en saksbehandler, fjern beskyttelsen og prøv igjen", cause).also {
            log.warn("Kaster ${it.javaClass.simpleName}")
        }

    class PassordBeskyttetException(msg: String, cause: Throwable?) : DokumentException(msg, cause, PASSWORD_PROTECTED)
}