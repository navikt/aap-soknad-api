package no.nav.aap.api.søknad.arkiv

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.STANDARD_ETTERSENDING
import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.søknad.arkiv.pdf.BildeTilPDFKonverterer
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.StandardEttersending
import no.nav.aap.api.søknad.model.StandardEttersending.EttersendtVedlegg
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
import no.nav.aap.api.søknad.arkiv.Journalpost.AvsenderMottaker
import no.nav.aap.api.søknad.arkiv.Journalpost.Bruker
import no.nav.aap.api.søknad.arkiv.Journalpost.Dokument
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant.Filtype.JSON
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant.Filtype.PDFA
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant.VariantFormat.ARKIV
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant.VariantFormat.ORIGINAL
import no.nav.aap.api.søknad.arkiv.pdf.PDFClient
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.callIdAsUUID
import no.nav.aap.util.StringExtensions.størrelse
import no.nav.aap.util.StringExtensions.toEncodedJson
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.stereotype.Component
import java.util.*

@Component
class ArkivJournalpostGenerator(
        private val mapper: ObjectMapper,
        private val lager: Dokumentlager,
        private val pdf: PDFClient,
        private val ctx: AuthContext,
        private val konverterer: BildeTilPDFKonverterer
) {

    private val log = getLogger(javaClass)

    fun journalpostFra(es: StandardEttersending, søker: Søker): Journalpost =
        Journalpost(
            dokumenter = dokumenterFra(es.ettersendteVedlegg, søker.fnr),
            tittel = STANDARD_ETTERSENDING.tittel, eksternReferanseId = callIdAsUUID(),
            avsenderMottaker = AvsenderMottaker(søker.fnr, navn = søker.navn.navn),
            bruker = Bruker(søker.fnr)
        )
            .also {
                log.trace("Journalpost med ${it.størrelse()} er $it")
            }

    private fun dokumenterFra(vedlegg: List<EttersendtVedlegg>, fnr: Fødselsnummer) =
        vedlegg.flatMap { e ->
            require(vedlegg.isNotEmpty()) { "Forventet > 0 vedlegg" }
            dokumenterFra(e.ettersending, e.vedleggType, fnr)
        }.also {
            require(it.isNotEmpty()) { "Forventet > 0 vedlegg fra dokumentlager" }
        }

    fun journalpostFra(søknad: UtlandSøknad, søker: Søker) =
        Journalpost(
            dokumenter = dokumenterFra(søknad,  pdf.tilPdf(søker,søknad).somPDFVariant()),
            tittel = UTLAND.tittel, eksternReferanseId = callIdAsUUID(),
            avsenderMottaker = AvsenderMottaker(søker.fnr, navn = søker.navn.navn),
            bruker = Bruker(søker.fnr))
            .also {
                log.trace("Journalpost med ${it.størrelse()} er $it")
            }

    fun journalpostFra(søknad: StandardSøknad, søker: Søker) =
        Journalpost(
            dokumenter = journalpostDokumenterFra(søknad, pdf.tilPdf(søker,søknad).somPDFVariant()),
            tittel = STANDARD.tittel, eksternReferanseId = callIdAsUUID(),
            avsenderMottaker = AvsenderMottaker(søker.fnr, navn = søker.navn.navn),
            bruker = Bruker(søker.fnr))
            .also {
                log.trace("Journalpost med ${it.størrelse()} er $it")
            }

    private fun journalpostDokumenterFra(søknad: StandardSøknad, pdfVariant: DokumentVariant) =
        with(søknad) {
            dokumenterFra(this, pdfVariant).apply {
                addAll(dokumenterFra(studier, STUDIER))
                addAll(dokumenterFra(andreBarn, ANDREBARN))
                addAll(dokumenterFra(utbetalinger?.ekstraFraArbeidsgiver, ARBEIDSGIVER))
                addAll(
                    dokumenterFra(
                        utbetalinger?.andreStønader?.find { it.type == AnnenStønadstype.UTLAND },
                        VedleggType.UTLAND
                    )
                )
                addAll(dokumenterFra(utbetalinger?.andreStønader?.find { it.type == OMSORGSSTØNAD }, OMSORG))
                addAll(dokumenterFra(this@with, ANNET))
            }.also {
                log.trace("Sender ${it.størrelse("dokument")} til arkiv  $it")
            }
        }

    private fun dokumenterFra(søknad: StandardSøknad, pdfVariant: DokumentVariant) =
        mutableListOf(Dokument(STANDARD, listOf(søknad.somJsonVariant(mapper), pdfVariant)))

    private fun dokumenterFra(a: List<VedleggAware?>?, type: VedleggType) =
        a?.map {
            dokumenterFra(it?.vedlegg, type)
        }?.flatten() ?: emptyList()

    private fun dokumenterFra(a: VedleggAware?, type: VedleggType): List<Dokument> =
        a?.let { v ->
            v.vedlegg?.let { dokumenterFra(it, type) }
        } ?: emptyList()

    private fun dokumenterFra(v: Vedlegg?, type: VedleggType) =
        dokumenterFra(v, type, ctx.getFnr())

    private fun dokumenterFra(v: Vedlegg?, type: VedleggType, fnr: Fødselsnummer) =
        v?.let { vl ->
            val vedlegg = (vl.deler?.mapNotNull {
                it?.let { uuid ->
                    log.trace("Leser dokument $uuid fra dokkumentlager")
                    lager.lesDokument(uuid, fnr)
                }
            } ?: emptyList())
                .sortedBy { it.createTime }
                .groupBy { it.contentType }
            val pdfs = vedlegg[APPLICATION_PDF_VALUE] ?: mutableListOf()
            val jpgs = vedlegg[IMAGE_JPEG_VALUE] ?: emptyList()
            val pngs = vedlegg[IMAGE_PNG_VALUE] ?: emptyList()
            pdfs.map { it.somDokument(type.tittel) }.toMutableList().apply {
                if (jpgs.isNotEmpty()) {
                    add(konverterer.tilPdf(IMAGE_JPEG_VALUE, jpgs.map(DokumentInfo::bytes)).somDokument(type.tittel))
                }
                if (pngs.isNotEmpty()) {
                    add(konverterer.tilPdf(IMAGE_PNG_VALUE, pngs.map(DokumentInfo::bytes)).somDokument(type.tittel))
                }
            }
        } ?: emptyList<Dokument>().also {
            log.trace("Ingen dokumenter å lese fra dokumentlager")
        }

    private fun dokumenterFra(søknad: UtlandSøknad, pdfDokument: DokumentVariant) =
        listOf(Dokument(UTLAND, listOf(søknad.somJsonVariant(mapper), pdfDokument)
            .also {
                log.trace("${it.størrelse("dokumentvariant")}) ($it)")
            }).also {
            log.trace("Dokument til arkiv $it")
        })

    private fun Journalpost.størrelse() = dokumenter.størrelse("dokument")
    private fun ByteArray.somDokument(tittel: String) =
        Dokument(tittel, DokumentVariant(PDFA, encode())).also {
            log.trace("Dokument konvertert er $it")
        }

    private fun DokumentInfo.somDokument(tittel: String) = bytes.somDokument(tittel)
    private fun ByteArray.somPDFVariant() = DokumentVariant(PDFA, encode(), ARKIV)
    private fun ByteArray.encode() = Base64.getEncoder().encodeToString(this)
    fun StandardSøknad.somJsonVariant(mapper: ObjectMapper) = DokumentVariant(JSON, toEncodedJson(mapper), ORIGINAL)
    fun UtlandSøknad.somJsonVariant(mapper: ObjectMapper) = DokumentVariant(JSON, toEncodedJson(mapper), ORIGINAL)


}