package no.nav.aap.api.søknad.joark

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.søknad.joark.pdf.Image2PDFConverter
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.api.søknad.model.Vedlegg
import no.nav.aap.api.søknad.model.VedleggAware
import no.nav.aap.joark.AvsenderMottaker
import no.nav.aap.joark.Bruker
import no.nav.aap.joark.Dokument
import no.nav.aap.joark.DokumentVariant
import no.nav.aap.joark.Filtype.PDFA
import no.nav.aap.joark.Journalpost
import no.nav.aap.joark.asPDFVariant
import no.nav.aap.util.LoggerUtil
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.stereotype.Component
import java.util.Base64.getEncoder

@Component
class JoarkConverter(
        private val mapper: ObjectMapper,
        private val lager: Dokumentlager,
        private val converter: Image2PDFConverter) {

    private val log = LoggerUtil.getLogger(javaClass)

    fun convert(søknad: UtlandSøknad, søker: Søker, pdf: ByteArray) =
        Journalpost(dokumenter = dokumenterFra(søknad, pdf.asPDFVariant()),
                tittel = UTLAND.tittel,
                avsenderMottaker = AvsenderMottaker(søker.fnr,
                        navn = søker.navn.navn),
                bruker = Bruker(søker.fnr))
            .also { log.trace("Journalpost er $it") }

    fun convert(søknad: StandardSøknad, søker: Søker, pdf: ByteArray) =
        Journalpost(dokumenter = dokumenterFra(søknad, søker, pdf.asPDFVariant()),
                tittel = STANDARD.tittel,
                avsenderMottaker = AvsenderMottaker(søker.fnr,
                        navn = søker.navn.navn),
                bruker = Bruker(søker.fnr))
            .also { log.trace("Journalpost med ${it.dokumenter.size} dokumenter er $it") }

    private fun dokumenterFra(søknad: StandardSøknad, søker: Søker, pdfVariant: DokumentVariant) =
        with(søknad) {
            dokumenterFra(this, pdfVariant).apply {
                addAll(dokumenterFra(studier, søker.fnr, "Dokumentasjon av studier"))
                addAll(dokumenterFra(utbetalinger?.ekstraUtbetaling, søker.fnr, "Dokumentasjon av ekstra utbetalinger"))
                addAll(dokumenterFra(utbetalinger?.ekstraFraArbeidsgiver,
                        søker.fnr,
                        "Dokumentasjon av ekstra utbetaling fra arbeidsgiver"))
                addAll(dokumenterFra(this@with, søker.fnr, "Annen dokumentasjon"))
                addAll(dokumenterFra(utbetalinger?.andreStønader, søker.fnr, "Dokumentasjon av andre stønader"))
                addAll(dokumenterFra(andreBarn, søker.fnr, "barn"))
            }.also { log.trace("Sender ${it.size} dokumenter til JOARK  $it") } //
        }

    private fun dokumenterFra(søknad: StandardSøknad, pdfVariant: DokumentVariant) =
        mutableListOf(Dokument(STANDARD, listOf(søknad.asJsonVariant(mapper), pdfVariant)))

    private fun dokumenterFra(a: List<VedleggAware?>?, fnr: Fødselsnummer, tittel: String?): List<Dokument> =
        a?.map { it ->
            dokumenterFra(it?.vedlegg, fnr, tittel)
        }?.flatten() ?: emptyList()

    private fun dokumenterFra(a: VedleggAware?, fnr: Fødselsnummer, tittel: String): List<Dokument> =
        a?.let { v ->
            v.vedlegg?.let { dokumenterFra(it, fnr, tittel) }
        } ?: emptyList()

    private fun dokumenterFra(v: Vedlegg?, fnr: Fødselsnummer, tittel: String?): List<Dokument> =
        v?.let { vl ->
            val alle = vl.deler?.mapNotNull { it?.let { it1 -> lager.lesDokument(fnr, it1) } } ?: emptyList()
            var vedlegg = alle.groupBy { it.contentType }
            val pdfs = vedlegg[APPLICATION_PDF_VALUE] ?: mutableListOf()
            val jpgs = vedlegg[IMAGE_JPEG_VALUE] ?: emptyList()
            val pngs = vedlegg[IMAGE_PNG_VALUE] ?: emptyList()
            pdfs.map { it.asDokument(tittel) }.toMutableList().apply {
                if (jpgs.isNotEmpty()) {
                    add(converter.convert(IMAGE_JPEG_VALUE, jpgs.map(DokumentInfo::bytes)).asDokument(tittel))
                }
                if (pngs.isNotEmpty()) {
                    add(converter.convert(IMAGE_PNG_VALUE, pngs.map(DokumentInfo::bytes)).asDokument(tittel))
                }
            }
        } ?: emptyList()

    private fun ByteArray.asDokument(tittel: String?) =
        Dokument(tittel = tittel,
                dokumentVariant = DokumentVariant(PDFA,
                        getEncoder().encodeToString(this))).also {
            log.trace("DokumentInfo konvertert fra bytes er $it")
        }

    private fun DokumentInfo.asDokument(tittel: String?) =
        Dokument(tittel = tittel,
                dokumentVariant = DokumentVariant(PDFA,
                        getEncoder().encodeToString(bytes)))
            .also { log.trace("DokumentInfo konvertert er $it") }

    private fun dokumenterFra(søknad: UtlandSøknad, pdfDokument: DokumentVariant) =
        listOf(Dokument(UTLAND,
                listOf(søknad.asJsonVariant(mapper), pdfDokument)
                    .also { log.trace("${it.size} dokumentvariant(er) ($it)") }))
            .also { log.trace("Dokument til JOARK $it") }
}