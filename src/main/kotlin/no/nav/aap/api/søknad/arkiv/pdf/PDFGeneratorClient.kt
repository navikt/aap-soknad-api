package no.nav.aap.api.søknad.arkiv.pdf

import org.springframework.stereotype.Component
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant
import no.nav.aap.api.oppslag.person.Søker
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.StringExtensions.encode

@Component
class PDFGeneratorClient(val adapter : PDFGeneratorWebClientAdapter) : PDFGenerator {

    private val log = LoggerUtil.getLogger(javaClass)

    override fun tilPdf(søker : Søker, kvitteringData : PDFKvittering) = adapter.generate(søker, kvitteringData).also {
        log.info("Kvittering timestamp ${kvitteringData.mottattdato}")
    }
}

interface PDFGenerator {

    fun tilPdf(søker : Søker, kvitteringData : PDFKvittering) : ByteArray
    fun pdfVariant(søknad : PDFKvittering, søker : Søker) : DokumentVariant = tilPdf(søker, søknad).somPDFVariant()

    private fun ByteArray.somPDFVariant() = DokumentVariant(encode())
}