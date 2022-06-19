package no.nav.aap.api.søknad.joark

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.storage.Blob
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.mellomlagring.Dokumentlager
import no.nav.aap.api.søknad.joark.pdf.PDFClient
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.api.søknad.model.VedleggAware
import no.nav.aap.joark.AvsenderMottaker
import no.nav.aap.joark.Bruker
import no.nav.aap.joark.Dokument
import no.nav.aap.joark.DokumentVariant
import no.nav.aap.joark.Filtype.Companion.of
import no.nav.aap.joark.Journalpost
import no.nav.aap.joark.asPDFVariant
import no.nav.aap.util.LoggerUtil
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.stereotype.Service
import java.util.Base64.getEncoder

@Service
class JoarkRouter(private val joark: JoarkClient,
                  private val pdf: PDFClient,
                  private val lager: Dokumentlager,
                  private val mapper: ObjectMapper) {

    private val log = LoggerUtil.getLogger(javaClass)
    fun route(søknad: StandardSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            Pair(lagrePdf(this, søker.fødselsnummer),
                    joark.journalfør(journalpostFra(søknad, søker, asPDFVariant()))
                        ?: throw IntegrationException("Kunne ikke journalføre søknad"))
        }.also { slettVedlegg(søknad, søker.fødselsnummer) }

    fun route(søknad: UtlandSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            Pair(lagrePdf(this, søker.fødselsnummer),
                    joark.journalfør(journalpostFra(søknad, søker, asPDFVariant()))
                        ?: throw IntegrationException("Kunne ikke journalføre søknad"))
        }

    private fun lagrePdf(bytes: ByteArray, fnr: Fødselsnummer) =
        lager.lagreDokument(fnr, bytes, APPLICATION_PDF_VALUE, "kvittering.pdf")

    private fun journalpostFra(søknad: StandardSøknad, søker: Søker, pdfVariant: DokumentVariant) =
        Journalpost(dokumenter = dokumenterFra(søknad, søker, pdfVariant),
                tittel = STANDARD.tittel,
                avsenderMottaker = AvsenderMottaker(søker.fødselsnummer,
                        navn = søker.navn.navn),
                bruker = Bruker(søker.fødselsnummer))
            .also { log.trace("Journalpost er $it") }

    private fun journalpostFra(søknad: UtlandSøknad, søker: Søker, pdfVariant: DokumentVariant) =
        Journalpost(dokumenter = dokumenterFra(søknad, pdfVariant),
                tittel = UTLAND.tittel,
                avsenderMottaker = AvsenderMottaker(søker.fødselsnummer,
                        navn = søker.navn.navn),
                bruker = Bruker(søker.fødselsnummer))
            .also { log.trace("Journalpost er $it") }

    private fun dokumenterFra(søknad: StandardSøknad, søker: Søker, pdfVariant: DokumentVariant) =
        with(søknad) outer@{
            with(søker) {
                (listOfNotNull(dokumentFra(this@outer, pdfVariant),
                        dokumentFra(utbetalinger?.ekstraUtbetaling, fødselsnummer),
                        dokumentFra(utbetalinger?.ekstraFraArbeidsgiver, fødselsnummer),
                        dokumentFra(studier, fødselsnummer))
                        + dokumenterFra(andreVedlegg, fødselsnummer)
                        + dokumenterFra(utbetalinger?.andreStønader, fødselsnummer)
                        + dokumenterFra(andreBarn, fødselsnummer)).also {
                    log.trace("${it.size} dokument(er) til JOARK $it")
                }
            }
        }

    private fun dokumentFra(søknad: StandardSøknad,
                            pdfVariant: DokumentVariant) =
        Dokument(STANDARD, listOf(søknad.asJsonVariant(mapper), pdfVariant))

    private fun dokumenterFra(søknad: UtlandSøknad, pdfDokument: DokumentVariant) =
        listOf(Dokument(UTLAND,
                listOf(søknad.asJsonVariant(mapper), pdfDokument)
                    .also { log.trace("${it.size} dokumentvarianter ($it)") }))
            .also { log.trace("Dokument til JOARK $it") }

    private fun dokumenterFra(a: List<VedleggAware>?, fnr: Fødselsnummer) =
        a?.map { it -> dokumentFra(it, fnr) } ?: listOf()

    private fun dokumentFra(a: VedleggAware?, fnr: Fødselsnummer) =
        a?.vedlegg?.let { uuid ->
            lager.lesDokument(fnr, uuid)?.asDokumentVariant()?.let { Dokument(dokumentVariant = it) }
        }

    fun slettVedlegg(søknad: StandardSøknad, fnr: Fødselsnummer) {
        with(søknad) {
            slett(utbetalinger?.ekstraFraArbeidsgiver, fnr)
            slett(utbetalinger?.ekstraUtbetaling, fnr)
            slett(utbetalinger?.andreStønader, fnr)
            slett(andreVedlegg, fnr)
            slett(studier, fnr)
            slett(andreBarn, fnr)
        }
    }

    private fun slett(a: List<VedleggAware>?, fnr: Fødselsnummer) =
        a?.forEach { slett(it, fnr) }

    private fun slett(a: VedleggAware?, fnr: Fødselsnummer) =
        a?.vedlegg?.let { lager.slettDokument(it, fnr) }

    private fun Blob.asDokumentVariant() = DokumentVariant(of(contentType), getEncoder().encodeToString(getContent()))

}