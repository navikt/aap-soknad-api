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
            Pair(lagreKvittering(this, søker.fnr),
                    joark.journalfør(journalpostFra(søknad, søker, asPDFVariant()))
                        ?: throw IntegrationException("Kunne ikke journalføre søknad"))
        }.also { slettVedlegg(søknad, søker.fnr) }

    fun route(søknad: UtlandSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            Pair(lagreKvittering(this, søker.fnr),
                    joark.journalfør(journalpostFra(søknad, søker, asPDFVariant()))
                        ?: throw IntegrationException("Kunne ikke journalføre søknad"))
        }

    private fun lagreKvittering(bytes: ByteArray, fnr: Fødselsnummer) =
        lager.lagreDokument(fnr, bytes, APPLICATION_PDF_VALUE, "kvittering.pdf")

    private fun journalpostFra(søknad: StandardSøknad, søker: Søker, pdfVariant: DokumentVariant) =
        Journalpost(dokumenter = dokumenterFra(søknad, søker, pdfVariant),
                tittel = STANDARD.tittel,
                avsenderMottaker = AvsenderMottaker(søker.fnr,
                        navn = søker.navn.navn),
                bruker = Bruker(søker.fnr))
            .also { log.trace("Journalpost med ${it.dokumenter.size} dokumenter er $it") }

    private fun dokumenterFra(søknad: StandardSøknad, søker: Søker, pdfVariant: DokumentVariant) =
        with(søknad) {
            with(søker) {
                (listOfNotNull(dokumentFra(søknad, pdfVariant),
                        dokumentFra(utbetalinger?.ekstraUtbetaling, fnr),
                        dokumentFra(utbetalinger?.ekstraFraArbeidsgiver, fnr),
                        dokumentFra(studier, fnr))
                        + dokumenterFra(andreVedlegg, fnr)
                        + dokumenterFra(utbetalinger?.andreStønader, fnr)
                        + dokumenterFra(andreBarn, fnr)).also {
                    log.trace("${it.size} dokument(er) til JOARK:  $it")
                }
            }
        }

    private fun dokumentFra(søknad: StandardSøknad,
                            pdfVariant: DokumentVariant) =
        Dokument(STANDARD, listOf(søknad.asJsonVariant(mapper), pdfVariant))

    private fun dokumenterFra(a: List<VedleggAware>?, fnr: Fødselsnummer) =
        a?.map { it -> dokumentFra(it, fnr) } ?: emptyList()

    private fun dokumentFra(a: VedleggAware?, fnr: Fødselsnummer) =
        a?.let { v ->
            log.trace("Leser vedlegg for $v")
            v.vedlegg?.let { uuid ->
                log.trace("Leser dokument for $uuid")
                lager.lesDokument(fnr, uuid)?.asDokument(v.tittel)
                    .also { doc -> log.trace("Dokument fra $a er $doc") }
            }
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
        a?.vedlegg?.let { uuid ->
            lager.slettDokument(uuid, fnr).also { log.info("Slettet dokument $uuid ($it)") }
        }

    private fun Blob.asDokument(tittel: String): Dokument {
        log.trace("Blob as document $this")
        return Dokument(tittel = tittel,
                dokumentVariant = DokumentVariant(of(contentType),
                        getEncoder().encodeToString(getContent()))).also { log.trace("Blok konvertert er $it") }

    }

    private fun journalpostFra(søknad: UtlandSøknad, søker: Søker, pdfVariant: DokumentVariant) =
        Journalpost(dokumenter = dokumenterFra(søknad, pdfVariant),
                tittel = UTLAND.tittel,
                avsenderMottaker = AvsenderMottaker(søker.fnr,
                        navn = søker.navn.navn),
                bruker = Bruker(søker.fnr))
            .also { log.trace("Journalpost er $it") }

    private fun dokumenterFra(søknad: UtlandSøknad, pdfDokument: DokumentVariant) =
        listOf(Dokument(UTLAND,
                listOf(søknad.asJsonVariant(mapper), pdfDokument)
                    .also { log.trace("${it.size} dokumentvariant(er) ($it)") }))
            .also { log.trace("Dokument til JOARK $it") }
}