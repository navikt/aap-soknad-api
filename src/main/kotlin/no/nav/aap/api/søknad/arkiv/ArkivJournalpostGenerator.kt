package no.nav.aap.api.søknad.arkiv

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.IMAGE_JPEG
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.stereotype.Component
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

@Component
class ArkivJournalpostGenerator(
    private val pdl : PDLClient,
    private val mapper : ObjectMapper,
    private val lager : Dokumentlager,
    private val pdf : PDFGenerator,
    private val konverterer : PDFFraBildeFKonverterer) {

    private val log = getLogger(javaClass)

    companion object {

        const val ROUTING = "routing"
    }

    fun journalpostFra(es : Ettersending, søker : Søker, tilVikafossen : Boolean) =
        with(søker) {
            Journalpost(STANDARD_ETTERSENDING.tittel,
                AvsenderMottaker(fnr, navn),
                Bruker(fnr),
                dokumenterFra(es.ettersendteVedlegg),
                listOf(Tilleggsopplysning(ROUTING, "$tilVikafossen")))
                .also {
                    log.trace("Journalpost med {} er {}", it.størrelse(), it.dokumenter)
                }
        }

    fun journalpostFra(innsending : Innsending, søker : Søker) =
        with(søker) {
            val tilVikafossen = pdl.harBeskyttetBarn(barn + innsending.andreBarn)
            Journalpost(STANDARD.tittel,
                AvsenderMottaker(fnr, navn),
                Bruker(fnr),
                journalpostDokumenterFra(innsending, this),
                listOf(Tilleggsopplysning("versjon", VERSJON), Tilleggsopplysning(ROUTING, "$tilVikafossen")))
                .also {
                    log.trace("Journalpost med {} er {}  {}", it.størrelse(), it.dokumenter, it.tilleggsopplysninger)
                }
        }

    private fun journalpostDokumenterFra(innsendng : Innsending, søker : Søker) =
        with(innsendng.søknad) {
            dokumenterFra(this, pdf.pdfVariant(innsendng.kvittering, søker)).apply {
                addAll(dokumenterFra(studier, STUDIER))
                addAll(dokumenterFra(andreBarn, ANDREBARN))
                addAll(dokumenterFra(utbetalinger?.ekstraFraArbeidsgiver, ARBEIDSGIVER))
                addAll(dokumenterFra(utbetalinger?.andreStønader?.find { it.type == UTLAND }, UTENLANDSKE))
                addAll(dokumenterFra(utbetalinger?.andreStønader?.find { it.type == OMSORGSSTØNAD }, OMSORG))
                addAll(dokumenterFra(utbetalinger?.andreStønader?.find { it.type == STIPEND }, LÅNEKASSEN_STIPEND))
                addAll(dokumenterFra(utbetalinger?.andreStønader?.find { it.type == LÅN }, LÅNEKASSEN_LÅN))
                addAll(dokumenterFra(this@with, ANNET))
            }.also {
                log.trace("Sender {} til arkiv {}", it.størrelse("dokument"), it)
            }
        }

    private fun dokumenterFra(vedlegg : List<EttersendtVedlegg>) =
        vedlegg.flatMap { e ->
            require(vedlegg.isNotEmpty()) { "Forventet > 0 vedlegg" }
            dokumenterFra(e.ettersending, e.vedleggType, STANDARD_ETTERSENDING)
        }.also {
            require(it.isNotEmpty()) { "Forventet > 0 vedlegg fra dokumentlager for ${vedlegg.map { v -> v.ettersending.deler }}" }
        }

    private fun dokumenterFra(søknad : AAPSøknad, pdfVariant : DokumentVariant) =
        mutableListOf(Dokument(listOf(søknad.somOriginal(mapper), pdfVariant)))

    private fun dokumenterFra(a : List<VedleggAware?>?, type : VedleggType) =
        a?.flatMap {
            dokumenterFra(it?.vedlegg, type)
        } ?: emptyList()

    private fun dokumenterFra(a : VedleggAware?, type : VedleggType) : List<Dokument> =
        a?.let { v ->
            v.vedlegg?.let { dokumenterFra(it, type) }
        } ?: emptyList()

    private fun dokumenterFra(v : Vedlegg?, type : VedleggType, skjemaType : SkjemaType? = null) =
        v?.let { vl ->
            val vedlegg = grupperteOgSorterteVedlegg(vl)
            val pdfs = vedlegg[APPLICATION_PDF_VALUE] ?: mutableListOf()
            val jpgs = vedlegg[IMAGE_JPEG_VALUE] ?: emptyList()
            val pngs = vedlegg[IMAGE_PNG_VALUE] ?: emptyList()
            pdfs.map { it.somDokument(type.tittel, skjemaType?.kode) }.toMutableList().apply {
                if (jpgs.isNotEmpty()) {
                    add(konverterer.tilPdf(IMAGE_JPEG, jpgs.map(DokumentInfo::bytes)).somDokument(type.tittel, skjemaType?.kode))
                }
                if (pngs.isNotEmpty()) {
                    add(konverterer.tilPdf(IMAGE_PNG, pngs.map(DokumentInfo::bytes)).somDokument(type.tittel, skjemaType?.kode))
                }
            }
        } ?: emptyList<Dokument>().also {
            log.trace("Ingen dokumenter å lese fra dokumentlager")
        }

    private fun grupperteOgSorterteVedlegg(vl : Vedlegg) = (vl.deler?.mapNotNull {
        it?.let {
            log.trace("Leser dokument {} fra dokkumentlager", it)
            lager.lesDokument(it)
        }
    } ?: emptyList())
        .sortedBy { it.createTime }
        .groupBy { it.contentType }

    private fun Journalpost.størrelse() = dokumenter.størrelse("dokument")
    private fun ByteArray.somDokument(tittel : String, brevkode : String? = null) = Dokument(tittel, brevkode, DokumentVariant(encode()))
    private fun DokumentInfo.somDokument(tittel : String, brevkode : String? = null) = bytes.somDokument(tittel, brevkode)
    fun AAPSøknad.somOriginal(mapper : ObjectMapper) = DokumentVariant(toEncodedJson(mapper), ORIGINAL, JSON)
}