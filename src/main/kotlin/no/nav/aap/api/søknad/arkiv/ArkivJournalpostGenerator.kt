package no.nav.aap.api.søknad.arkiv

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.STANDARD_ETTERSENDING
import no.nav.aap.api.oppslag.person.PDLClient
import no.nav.aap.api.oppslag.person.Søker
import no.nav.aap.api.søknad.arkiv.Journalpost.AvsenderMottaker
import no.nav.aap.api.søknad.arkiv.Journalpost.Bruker
import no.nav.aap.api.søknad.arkiv.Journalpost.Dokument
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant.Filtype.JSON
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant.VariantFormat.ORIGINAL
import no.nav.aap.api.søknad.arkiv.Journalpost.Tilleggsopplysning
import no.nav.aap.api.søknad.arkiv.pdf.PDFFraBildeFKonverterer
import no.nav.aap.api.søknad.arkiv.pdf.PDFGenerator
import no.nav.aap.api.søknad.fordeling.AAPSøknad
import no.nav.aap.api.søknad.fordeling.AAPSøknad.Companion.VERSJON
import no.nav.aap.api.søknad.fordeling.Ettersending
import no.nav.aap.api.søknad.fordeling.Ettersending.EttersendtVedlegg
import no.nav.aap.api.søknad.fordeling.Innsending
import no.nav.aap.api.søknad.fordeling.Utbetalinger.AnnenStønadstype.LÅN
import no.nav.aap.api.søknad.fordeling.Utbetalinger.AnnenStønadstype.OMSORGSSTØNAD
import no.nav.aap.api.søknad.fordeling.Utbetalinger.AnnenStønadstype.STIPEND
import no.nav.aap.api.søknad.fordeling.Utbetalinger.AnnenStønadstype.UTLAND
import no.nav.aap.api.søknad.fordeling.Vedlegg
import no.nav.aap.api.søknad.fordeling.VedleggAware
import no.nav.aap.api.søknad.fordeling.VedleggType
import no.nav.aap.api.søknad.fordeling.VedleggType.ANDREBARN
import no.nav.aap.api.søknad.fordeling.VedleggType.ANNET
import no.nav.aap.api.søknad.fordeling.VedleggType.ARBEIDSGIVER
import no.nav.aap.api.søknad.fordeling.VedleggType.LÅNEKASSEN_LÅN
import no.nav.aap.api.søknad.fordeling.VedleggType.LÅNEKASSEN_STIPEND
import no.nav.aap.api.søknad.fordeling.VedleggType.OMSORG
import no.nav.aap.api.søknad.fordeling.VedleggType.STUDIER
import no.nav.aap.api.søknad.fordeling.VedleggType.UTENLANDSKE
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.encode
import no.nav.aap.util.StringExtensions.størrelse
import no.nav.aap.util.StringExtensions.toEncodedJson
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.stereotype.Component

@Component
class ArkivJournalpostGenerator(
    private val pdl: PDLClient,
    private val mapper: ObjectMapper,
    private val lager: Dokumentlager,
    private val pdf: PDFGenerator,
    private val konverterer: PDFFraBildeFKonverterer
) {

    private val log = getLogger(javaClass)

    companion object {

        const val ROUTING = "routing"
    }

    fun journalpostFra(es: Ettersending, søker: Søker, tilVikafossen: Boolean) =
        Journalpost(
            STANDARD_ETTERSENDING.tittel,
            AvsenderMottaker(søker.fnr, søker.navn),
            Bruker(søker.fnr),
            dokumenterFra(es.ettersendteVedlegg),
            listOf(Tilleggsopplysning(ROUTING, "$tilVikafossen"))
        ).also {
            log.trace("Journalpost med {} er {}", it.størrelse(), it.dokumenter)
        }

    fun journalpostFra(innsending: Innsending, søker: Søker): Journalpost {
        val tilVikafossen = pdl.harBeskyttetBarn(søker.barn + innsending.andreBarn)
        return Journalpost(
            STANDARD.tittel,
            AvsenderMottaker(søker.fnr, søker.navn),
            Bruker(søker.fnr),
            journalpostDokumenterFra(innsending, søker),
            listOf(Tilleggsopplysning("versjon", VERSJON), Tilleggsopplysning(ROUTING, "$tilVikafossen"))
        ).also {
            log.trace("Journalpost med {} er {}  {}", it.størrelse(), it.dokumenter, it.tilleggsopplysninger)
        }
    }

    private fun journalpostDokumenterFra(innsendng: Innsending, søker: Søker): List<Dokument> {
        return dokumenterFra(innsendng.søknad, pdf.pdfVariant(innsendng.kvittering, søker)) +
                dokumenterFra(innsendng.søknad.studier, STUDIER) +
                dokumenterFra(innsendng.søknad.andreBarn, ANDREBARN) +
                dokumenterFra(innsendng.søknad.utbetalinger?.ekstraFraArbeidsgiver, ARBEIDSGIVER) +
                dokumenterFra(innsendng.søknad.utbetalinger?.andreStønader?.find { it.type == UTLAND }, UTENLANDSKE) +
                dokumenterFra(innsendng.søknad.utbetalinger?.andreStønader?.find { it.type == OMSORGSSTØNAD }, OMSORG) +
                dokumenterFra(
                    innsendng.søknad.utbetalinger?.andreStønader?.find { it.type == STIPEND },
                    LÅNEKASSEN_STIPEND
                ) +
                dokumenterFra(innsendng.søknad.utbetalinger?.andreStønader?.find { it.type == LÅN }, LÅNEKASSEN_LÅN) +
                dokumenterFra(innsendng.søknad, ANNET)
    }

    private fun dokumenterFra(vedlegg: List<EttersendtVedlegg>) =
        vedlegg.flatMap { e ->
            require(vedlegg.isNotEmpty()) { "Forventet > 0 vedlegg" }
            dokumenterFra(e.ettersending, e.vedleggType, STANDARD_ETTERSENDING)
        }.also {
            require(it.isNotEmpty()) { "Forventet > 0 vedlegg fra dokumentlager for ${vedlegg.map { v -> v.ettersending.deler }}" }
        }

    private fun dokumenterFra(søknad: AAPSøknad, pdfVariant: DokumentVariant) =
        mutableListOf(Dokument(listOf(søknad.somOriginal(mapper), pdfVariant)))

    private fun dokumenterFra(a: List<VedleggAware?>?, type: VedleggType) =
        a?.flatMap {
            dokumenterFra(it?.vedlegg, type)
        } ?: emptyList()

    private fun dokumenterFra(a: VedleggAware?, type: VedleggType): List<Dokument> =
        a?.let { v ->
            v.vedlegg?.let { dokumenterFra(it, type) }
        } ?: emptyList()

    private fun dokumenterFra(vedlegg: Vedlegg?, type: VedleggType, skjemaType: SkjemaType? = null): List<Dokument> {
        if (vedlegg == null) {
            return emptyList()
        }
        val grupperteVedlegg = grupperteOgSorterteVedlegg(vedlegg)
        val pdfs = grupperteVedlegg[APPLICATION_PDF_VALUE] ?: mutableListOf()
        val jpgs = grupperteVedlegg[IMAGE_JPEG_VALUE] ?: emptyList()
        val pngs = grupperteVedlegg[IMAGE_PNG_VALUE] ?: emptyList()

        val bilder = jpgs + pngs
        var bildePdf = emptyList<Dokument>()

        if (bilder.isNotEmpty()) {
            bildePdf =
                listOf(konverterer.tilPdf(bilder.map(DokumentInfo::bytes)).somDokument(type.tittel, skjemaType?.kode))
        }
        return pdfs.map { it.somDokument(type.tittel, skjemaType?.kode) } + bildePdf
    }

    private fun grupperteOgSorterteVedlegg(vl: Vedlegg): Map<String, List<DokumentInfo>> {
        return vl.deler.mapNotNull {
            it.let {
                log.trace("Leser dokument {} fra dokkumentlager", it)
                lager.lesDokument(it)
            }
        }.sortedBy { it.createTime }
            .groupBy { it.contentType }
    }

    private fun Journalpost.størrelse() = dokumenter.størrelse("dokument")
    private fun ByteArray.somDokument(tittel: String, brevkode: String? = null) =
        Dokument(tittel, brevkode, DokumentVariant(encode()))

    private fun DokumentInfo.somDokument(tittel: String, brevkode: String? = null) = bytes.somDokument(tittel, brevkode)
    fun AAPSøknad.somOriginal(mapper: ObjectMapper) = DokumentVariant(toEncodedJson(mapper), ORIGINAL, JSON)
}