package no.nav.aap.api.søknad.joark

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.storage.Blob
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.mellomlagring.Vedlegg
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.SkjemaType.HOVED
import no.nav.aap.api.søknad.formidling.SøknadFormidler
import no.nav.aap.api.søknad.joark.pdf.PDFGenerator
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.Utbetaling.VedleggAware
import no.nav.aap.joark.AvsenderMottaker
import no.nav.aap.joark.Bruker
import no.nav.aap.joark.Dokument
import no.nav.aap.joark.DokumentVariant
import no.nav.aap.joark.Filtype.Companion.of
import no.nav.aap.joark.Filtype.JSON
import no.nav.aap.joark.Filtype.PDFA
import no.nav.aap.joark.JoarkResponse
import no.nav.aap.joark.Journalpost
import no.nav.aap.joark.VariantFormat.ORIGINAL
import no.nav.aap.util.AuthContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.stereotype.Service
import java.util.*
import java.util.Base64.getEncoder

@Service
class JoarkFormidler(private val joark: JoarkClient,
                     private val pdf: PDFGenerator,
                     private val ctx: AuthContext,
                     private val vedlegg: Vedlegg) : SøknadFormidler<Pair<UUID, JoarkResponse>> {

    @Autowired
    private lateinit var mapper: ObjectMapper

    override fun formidle(søknad: StandardSøknad, søker: Søker) =
         with(pdf.generate(søker, søknad)) {
            Pair(lagreDokument(this),
            joark.journalfør(journalpostFra(søknad, søker,this.asPDFVariant())) ?: throw IntegrationException("Kunne ikke journalføre søknad"))
        }
    private fun lagreDokument(bytes: ByteArray) = vedlegg.lagreDokument(ctx.getFnr(), bytes, APPLICATION_PDF_VALUE, "kvittering.pdf")

    private fun journalpostFra(søknad: StandardSøknad, søker: Søker, pdfDokument: DokumentVariant) =
        Journalpost(dokumenter = dokumenterFra(søknad, søker,pdfDokument),
                tittel = HOVED.tittel,
                avsenderMottaker = AvsenderMottaker(søker.fødselsnummer, navn = søker.navn.navn),
                bruker = Bruker(søker.fødselsnummer))

    private fun dokumenterFra(søknad: StandardSøknad, søker: Søker,pdfDokument: DokumentVariant) =
        listOf(Dokument(HOVED.tittel,
                HOVED.kode,
                listOf(jsonDokument(søknad), pdfDokument)
                        + vedleggFor(søknad.utbetalinger?.stønadstyper, søker.fødselsnummer)
                        + vedleggFor(søknad.utbetalinger?.andreUtbetalinger, søker.fødselsnummer)))

    private fun jsonDokument(søknad: StandardSøknad) = DokumentVariant(JSON, søknad.toEncodedJson(mapper), ORIGINAL)

    private fun ByteArray.asPDFVariant() = DokumentVariant(PDFA, getEncoder().encodeToString(this))

    private fun vedleggFor(utbetalinger: List<VedleggAware>?, fnr: Fødselsnummer) =
        utbetalinger
            ?.mapNotNull { it.hentVedlegg() }
            ?.mapNotNull { vedlegg.lesVedlegg(fnr, it) }
            ?.map { it.dokumentVariant() }
            .orEmpty()

    private fun Blob.dokumentVariant() = DokumentVariant(of(contentType), getEncoder().encodeToString(getContent()))
}