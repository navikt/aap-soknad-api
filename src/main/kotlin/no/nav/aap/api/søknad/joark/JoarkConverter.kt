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
import java.util.*

@Component
class JoarkConverter(
        private val mapper: ObjectMapper,
        private val lager: Dokumentlager,
        private val pdfConverter: Image2PDFConverter) {

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
                addAll(dokumenterFra(studier, søker.fnr))
                addAll(dokumenterFra(utbetalinger?.ekstraUtbetaling, søker.fnr))
                addAll(dokumenterFra(utbetalinger?.ekstraFraArbeidsgiver, søker.fnr))
                addAll(dokumenterFra(this@with, søker.fnr))
                addAll(dokumenterFra(utbetalinger?.andreStønader, søker.fnr))
                addAll(dokumenterFra(andreBarn, søker.fnr))
            }.also { log.trace("Sender ${it.size} dokumenter til JOARK  $it") }
        }

    private fun dokumenterFra(søknad: StandardSøknad, pdfVariant: DokumentVariant) =
        mutableListOf(Dokument(STANDARD, listOf(søknad.asJsonVariant(mapper), pdfVariant)))

    private fun dokumenterFra(a: List<VedleggAware?>?, fnr: Fødselsnummer): List<Dokument> =
        a?.let { it ->
            dokumenterFra(it, fnr)
        } ?: emptyList()

    private fun dokumenterFra(a: VedleggAware?, fnr: Fødselsnummer): List<Dokument> =
        a?.let { v ->
            v.vedlegg?.let { dokumenterFra(it, fnr) }
        } ?: emptyList()

    private fun dokumenterFra(v: Vedlegg, fnr: Fødselsnummer): List<Dokument> =
        v.let { vl ->
            vl.deler?.mapNotNull { uuid -> dokumentFra(uuid, v.tittel, fnr) }
        } ?: emptyList()

    private fun dokumentFra(uuid: UUID?, tittel: String?, fnr: Fødselsnummer): Dokument? =
        uuid?.let {
            lager.lesDokument(fnr, it)?.asDokument(tittel).also { doc ->
                log.trace("Dokument fra $it er $doc")
            }
        }

    private fun DokumentInfo.asDokument(tittel: String?) =
        Dokument(tittel = tittel,
                dokumentVariant = DokumentVariant(PDFA,
                        Base64.getEncoder().encodeToString(when (contentType) {
                            APPLICATION_PDF_VALUE -> bytes
                            IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE -> pdfConverter.convert(bytes)
                            else -> throw IllegalStateException("UKjent content type $contentType, skal ikke skje")
                        }))).also { log.trace("Blob konvertert er $it") }

    private fun dokumenterFra(søknad: UtlandSøknad, pdfDokument: DokumentVariant) =
        listOf(Dokument(UTLAND,
                listOf(søknad.asJsonVariant(mapper), pdfDokument)
                    .also { log.trace("${it.size} dokumentvariant(er) ($it)") }))
            .also { log.trace("Dokument til JOARK $it") }
}