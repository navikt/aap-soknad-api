package no.nav.aap.api.søknad.joark

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.søknad.joark.pdf.BildeTilPDFKonverterer
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.Utbetalinger.AnnenStønadstype
import no.nav.aap.api.søknad.model.Utbetalinger.AnnenStønadstype.OMSORGSSTØNAD
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.api.søknad.model.Vedlegg
import no.nav.aap.api.søknad.model.VedleggAware
import no.nav.aap.api.søknad.model.VedleggType
import no.nav.aap.api.søknad.model.VedleggType.ANDREBARN
import no.nav.aap.api.søknad.model.VedleggType.ANNET
import no.nav.aap.api.søknad.model.VedleggType.ARBEIDSGIVER
import no.nav.aap.api.søknad.model.VedleggType.OMSORG
import no.nav.aap.api.søknad.model.VedleggType.STUDIER
import no.nav.aap.joark.AvsenderMottaker
import no.nav.aap.joark.Bruker
import no.nav.aap.joark.Dokument
import no.nav.aap.joark.DokumentVariant
import no.nav.aap.joark.Filtype.PDFA
import no.nav.aap.joark.Journalpost
import no.nav.aap.joark.asPDFVariant
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.stereotype.Component
import java.util.Base64.getEncoder

@Component
class JoarkJournalpostGenerator(
        private val mapper: ObjectMapper,
        private val lager: Dokumentlager,
        private val konverterer: BildeTilPDFKonverterer) {

    private val log = getLogger(javaClass)

    fun journalpostFra(søknad: UtlandSøknad, søker: Søker, pdf: ByteArray) =
        Journalpost(dokumenter = dokumenterFra(søknad, pdf.asPDFVariant()),
                tittel = UTLAND.tittel,
                avsenderMottaker = AvsenderMottaker(søker.fnr, navn = søker.navn.navn),
                bruker = Bruker(søker.fnr))
            .also {
                log.trace("Journalpost med ${it.dokumenter.size} dokumenter er $it")
            }

    fun journalpostFra(søknad: StandardSøknad, søker: Søker, pdf: ByteArray) =
        Journalpost(dokumenter = dokumenterFra(søknad, søker, pdf.asPDFVariant()),
                tittel = STANDARD.tittel,
                avsenderMottaker = AvsenderMottaker(søker.fnr, navn = søker.navn.navn),
                bruker = Bruker(søker.fnr))
            .also {
                log.trace("Journalpost med ${it.dokumenter.size} dokumenter er $it")
            }

    private fun dokumenterFra(søknad: StandardSøknad, søker: Søker, pdfVariant: DokumentVariant) =
        with(søknad) {
            dokumenterFra(this, pdfVariant).apply {
                log.trace("Etter pdf og json,størrelse er $size")
                addAll(dokumenterFra(studier, STUDIER.tittel))
                log.trace("Etter studier, størrelse er $size")
                addAll(dokumenterFra(andreBarn, ANDREBARN.tittel))
                log.trace("Etter andre barn, størrelse er $size")
                addAll(dokumenterFra(utbetalinger?.ekstraFraArbeidsgiver, ARBEIDSGIVER.tittel))
                log.trace("Etter arbeidsgiver, størrelse er $size")
                addAll(dokumenterFra(utbetalinger?.andreStønader?.find { it.type == AnnenStønadstype.UTLAND },
                        VedleggType.UTLAND.tittel))
                log.trace("Etter utland, størrelse er $size")
                addAll(dokumenterFra(utbetalinger?.andreStønader?.find { it.type == OMSORGSSTØNAD },
                        OMSORG.tittel))
                log.trace("Etter omsorg, størrelse er $size")
                //  addAll(dokumenterFra(utbetalinger?.andreStønader, "Dokumentasjon av andre stønader"))
                addAll(dokumenterFra(this@with, ANNET.tittel))
                log.trace("Etter annet, størrelse er $size")
            }.also {
                log.trace("Sender ${it.size} dokumenter til JOARK  $it")
            }
        }

    private fun dokumenterFra(søknad: StandardSøknad, pdfVariant: DokumentVariant) =
        mutableListOf(Dokument(STANDARD, listOf(søknad.asJsonVariant(mapper), pdfVariant)))

    private fun dokumenterFra(a: List<VedleggAware?>?, tittel: String?) =
        a?.map {
            dokumenterFra(it?.vedlegg, tittel)
        }?.flatten() ?: emptyList()

    private fun dokumenterFra(a: VedleggAware?, tittel: String): List<Dokument> =
        a?.let { v ->
            v.vedlegg?.let { dokumenterFra(it, tittel) }
        } ?: emptyList()

    private fun dokumenterFra(v: Vedlegg?, tittel: String?) =
        v?.let { vl ->
            val vedlegg = (vl.deler?.mapNotNull {
                it?.let { uuid ->
                    lager.lesDokument(uuid)
                }
            } ?: emptyList())
                .sortedBy { it.createTime }
                .groupBy { it.contentType }
            val pdfs = vedlegg[APPLICATION_PDF_VALUE] ?: mutableListOf()
            val jpgs = vedlegg[IMAGE_JPEG_VALUE] ?: emptyList()
            val pngs = vedlegg[IMAGE_PNG_VALUE] ?: emptyList()
            pdfs.map { it.somDokument(tittel) }.toMutableList().apply {
                if (jpgs.isNotEmpty()) {
                    add(konverterer.tilPdf(IMAGE_JPEG_VALUE, jpgs.map(DokumentInfo::bytes)).somDokument(tittel))
                }
                if (pngs.isNotEmpty()) {
                    add(konverterer.tilPdf(IMAGE_PNG_VALUE, pngs.map(DokumentInfo::bytes)).somDokument(tittel))
                }
            }
        } ?: emptyList()

    private fun ByteArray.somDokument(tittel: String?) =
        Dokument(tittel = tittel,
                dokumentVariant = DokumentVariant(PDFA, getEncoder().encodeToString(this))).also {
            log.trace("Dokument konvertert fra bytes er $it")
        }

    private fun DokumentInfo.somDokument(tittel: String?) =
        Dokument(tittel = tittel, dokumentVariant = DokumentVariant(PDFA, getEncoder().encodeToString(bytes)))
            .also {
                log.trace("Dokument konvertert fra DokumentInfo er $it")
            }

    private fun dokumenterFra(søknad: UtlandSøknad, pdfDokument: DokumentVariant) =
        listOf(Dokument(UTLAND, listOf(søknad.asJsonVariant(mapper), pdfDokument)
            .also {
                log.trace("${it.size} dokumentvariant(er) ($it)")
            }).also {
            log.trace("Dokument til JOARK $it")
        })
}