package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant
import no.nav.aap.api.søknad.model.PDFKvittering
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.StringExtensions.encode
import org.springframework.stereotype.Component

@Component
class PDFGeneratorClient(val adapter: PDFGeneratorWebClientAdapter) : PDFGenerator {
    override fun tilPdf(søker: Søker, søknad: UtlandSøknad) = adapter.generate(søker, søknad)
    override fun tilPdf(søker: Søker, kvitteringData: PDFKvittering) = adapter.generate(søker, kvitteringData)
}

interface PDFGenerator {
    fun tilPdf(søker: Søker, søknad: PDFKvittering): ByteArray
    fun pdfVariant(søknad: PDFKvittering, søker: Søker): DokumentVariant = tilPdf(søker, søknad).somPDFVariant()

    fun tilPdf(søker: Søker, søknad: UtlandSøknad): ByteArray

    fun pdfVariant(søker: Søker, søknad: UtlandSøknad): DokumentVariant = tilPdf(søker, søknad).somPDFVariant()

    private fun ByteArray.somPDFVariant() = DokumentVariant(encode())

}