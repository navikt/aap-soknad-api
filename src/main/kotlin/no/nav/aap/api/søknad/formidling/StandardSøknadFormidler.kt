package no.nav.aap.api.søknad.formidling

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.storage.Blob
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.mellomlagring.Vedlegg
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.formidling.SkjemaType.HOVED
import no.nav.aap.api.søknad.joark.JoarkClient
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
import no.nav.aap.joark.Journalpost
import no.nav.aap.joark.VariantFormat.ORIGINAL
import no.nav.aap.util.LoggerUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.Base64.getEncoder

@Component
class StandardSøknadFormidler(private val joark: JoarkClient,
                              private val pdf: PDFGenerator,
                              private val pdl: PDLClient,
                              private val bucket: Vedlegg,
                              private val kafka: StandardSøknadKafkaFormidler) {

    @Autowired
    private lateinit var mapper: ObjectMapper
    private val log = LoggerUtil.getLogger(javaClass)

    fun formidle(søknad: StandardSøknad) =
        with(pdl.søkerMedBarn()) {
            joark.opprettJournalpost(
                    Journalpost(
                            dokumenter = dokumenterFra(this, søknad),
                            tittel = HOVED.tittel,
                            avsenderMottaker = AvsenderMottaker(fødselsnummer, navn = navn.navn),
                            bruker = Bruker(fødselsnummer)))
                .also { log.info("Journalført søknad $it OK") }
            kafka.formidle(this, søknad)
        }

    private fun dokumenterFra(søker: Søker, søknad: StandardSøknad) =
        listOf(Dokument(
                HOVED.tittel, HOVED.kode, listOf(
                jsonDokument(søknad),
                pdfDokument(søker, søknad))
                + vedleggFor(søknad.utbetalinger?.stønadstyper, søker.fødselsnummer)
                + vedleggFor(søknad.utbetalinger?.andreUtbetalinger, søker.fødselsnummer)))

    private fun jsonDokument(søknad: StandardSøknad) = DokumentVariant(JSON, søknad.toEncodedJson(mapper), ORIGINAL)
    private fun pdfDokument(søker: Søker, søknad: StandardSøknad) = DokumentVariant(PDFA, pdf.generate(søker, søknad))

    private fun vedleggFor(utbetalinger: List<VedleggAware>?, søker: Fødselsnummer) =
        utbetalinger
            ?.mapNotNull { it.hentVedlegg() }
            ?.mapNotNull { bucket.lesVedlegg(søker, it) }
            ?.map { it.dokumentVariant() }
            .orEmpty()

    private fun Blob.dokumentVariant() = DokumentVariant(of(contentType), getEncoder().encodeToString(getContent()))

}