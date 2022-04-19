package no.nav.aap.api.søknad.formidling

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.storage.Blob
import no.nav.aap.api.mellomlagring.GCPVedlegg
import no.nav.aap.api.mellomlagring.Vedlegg
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.formidling.SkjemaType.HOVED
import no.nav.aap.api.søknad.joark.JoarkClient
import no.nav.aap.api.søknad.joark.pdf.PDFGenerator
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
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
import java.util.Base64

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
        with(pdl.søkerUtenBarn()) {
            joark.opprettJournalpost(
                    Journalpost(
                            dokumenter = docs(this, søknad).also { log.info("Journalfører ${it.size} dokumenter") },
                            tittel = HOVED.tittel,
                            avsenderMottaker = AvsenderMottaker(fødselsnummer, navn = navn.navn),
                            bruker = Bruker(fødselsnummer)))
                .also { log.info("Journalført søknad $it OK") }
            kafka.formidle(søknad, this)
                .also { log.info("Formidlet søknad til Kakfa OK") }
        }

    private fun docs(søker: Søker, søknad: StandardSøknad) =
        listOf(Dokument(
                HOVED.tittel, HOVED.kode, listOf(
                DokumentVariant(JSON, søknad.toEncodedJson(mapper), ORIGINAL),
                DokumentVariant(PDFA, pdf.generate(søker, søknad)))
                + andreStønaderVedlegg(søknad, søker)
                + andreUtbetalingerVedlegg(søknad, søker)))

    private fun andreStønaderVedlegg(søknad: StandardSøknad, søker: Søker) =
        søknad.utbetalinger?.stønadstyper
            ?.mapNotNull { it.vedlegg }
            ?.map {
                with(bucket.lesVedlegg(søker.fødselsnummer, it)) {
                    DokumentVariant(of(contentType), encode())
                }
            }
            .orEmpty()

    private fun andreUtbetalingerVedlegg(søknad: StandardSøknad, søker: Søker) =
        søknad.utbetalinger?.andreUtbetalinger
            ?.mapNotNull { it.vedlegg }
            ?.map {
                with(bucket.lesVedlegg(søker.fødselsnummer, it)) {
                    DokumentVariant(of(contentType), encode())
                }
            }
            .orEmpty()

    private fun Blob.encode() = Base64.getEncoder().encodeToString(getContent())

}