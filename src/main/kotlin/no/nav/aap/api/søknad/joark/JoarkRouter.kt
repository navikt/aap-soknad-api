package no.nav.aap.api.søknad.joark

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.joark.pdf.PDFClient
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.api.søknad.model.VedleggAware
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Service
import java.util.*

@Service
class JoarkRouter(private val joark: JoarkClient,
                  private val pdf: PDFClient,
                  private val lager: Dokumentlager,
                  private val joarkConverter: JoarkConverter) {

    private val log = getLogger(javaClass)
    fun route(søknad: StandardSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            Pair(lagreKvittering(this, søker.fnr),
                    joark.journalfør(joarkConverter.convert(søknad, søker, this))
                        ?: throw IntegrationException("Kunne ikke journalføre søknad"))
        }.also { slettDokumenter(søknad, søker.fnr) }

    fun route(søknad: UtlandSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            Pair(lagreKvittering(this, søker.fnr),
                    joark.journalfør(joarkConverter.convert(søknad, søker, this))
                        ?: throw IntegrationException("Kunne ikke journalføre søknad"))
        }

    private fun lagreKvittering(bytes: ByteArray, fnr: Fødselsnummer) =
        lager.lagreDokument(fnr, DokumentInfo(bytes, "kvittering.pdf"))

    fun slettDokumenter(søknad: StandardSøknad, fnr: Fødselsnummer) {
        with(søknad) {
            slett(utbetalinger?.ekstraFraArbeidsgiver, fnr)
            slett(utbetalinger?.ekstraUtbetaling, fnr)
            slett(utbetalinger?.andreStønader, fnr)
            slett(this, fnr)
            slett(studier, fnr)
            slett(andreBarn, fnr)
        }
    }

    private fun slett(a: List<VedleggAware>?, fnr: Fødselsnummer) =
        a?.forEach { slett(it, fnr) }

    private fun slett(a: VedleggAware?, fnr: Fødselsnummer) =
        a?.vedlegg?.let {
            slettUUIDs(it.deler, fnr)
        }

    private fun slettUUIDs(uuids: List<UUID?>?, fnr: Fødselsnummer) =
        uuids?.forEach { slett(it, fnr) }

    private fun slett(uuid: UUID?, fnr: Fødselsnummer) =
        uuid?.let { lager.slettDokument(it, fnr).also { log.info("Slettet dokument $it") } }
}