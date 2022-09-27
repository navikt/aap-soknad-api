package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.DokumentException.Substatus.PASSWORD_PROTECTED
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.stereotype.Component

@Component
class PDFPassordSjekker : DokumentSjekker {
    private val log = getLogger(javaClass)

    override fun sjekk(dokument: DokumentInfo) =
        with(dokument) {
            if (APPLICATION_PDF_VALUE == contentType) {
                runCatching {
                    log.trace(CONFIDENTIAL, "Sjekker om  $filnavn er passord-beskyttet")
                    PDDocument.load(bytes).use { }
                }.getOrElse {
                    if (it is InvalidPasswordException) {
                        throw PassordBeskyttetException("$filnavn er passord-beskyttet", it)
                    }
                }
            }
            else {
                log.trace(CONFIDENTIAL, "Sjekker ikke $contentType for passord-beskyttelse")
            }
        }
}

class PassordBeskyttetException(msg: String, cause: Exception) : DokumentException(msg, cause, PASSWORD_PROTECTED)