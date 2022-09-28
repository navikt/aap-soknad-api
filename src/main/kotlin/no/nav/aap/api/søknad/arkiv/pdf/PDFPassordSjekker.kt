package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.DokumentException.Substatus.PASSWORD_PROTECTED
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Component
class PDFPassordSjekker : DokumentSjekker {
    override fun sjekk(dokument: DokumentInfo) =
        if (MediaType.APPLICATION_PDF_VALUE == dokument.contentType) {
            with(dokument) {
                runCatching {
                    PDDocument.load(bytes).use { }
                }.getOrElse {
                    if (it is InvalidPasswordException) {
                        throw PassordBeskyttetException("$filnavn er passord-beskyttet", it)
                    }
                }
            }
        }
        else Unit
}


class PassordBeskyttetException(msg: String, cause: Exception) : DokumentException(msg, cause, PASSWORD_PROTECTED)