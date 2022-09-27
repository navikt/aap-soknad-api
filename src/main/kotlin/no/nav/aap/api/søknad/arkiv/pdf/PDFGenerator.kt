package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.PDFKvittering
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.StringExtensions.encode
import org.springframework.stereotype.Component

@Component
class PDFGenerator(val a: PDFGeneratorWebClientAdapter) : PDFClient {
    override fun tilPdf(søker: Søker, søknad: UtlandSøknad) = a.generate(søker, søknad)
    override fun tilPdf(søker: Søker, PDFKvittering: PDFKvittering) = a.generate(søker, PDFKvittering)
}

interface PDFClient {
    fun tilPdf(søker: Søker, søknad: PDFKvittering): ByteArray
    fun somPdfVariant(søker: Søker, søknad: PDFKvittering): DokumentVariant = tilPdf(søker,søknad).somPDFVariant()

    fun tilPdf(søker: Søker, søknad: UtlandSøknad): ByteArray

    fun somPdfVariant(søker: Søker, søknad: UtlandSøknad): DokumentVariant = tilPdf(søker,søknad).somPDFVariant()

    private fun ByteArray.somPDFVariant() = DokumentVariant(encode())

}