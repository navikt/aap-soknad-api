package no.nav.aap.api.søknad.fordeling

import java.util.*
import java.util.UUID.randomUUID
import no.nav.aap.api.config.Metrikker
import no.nav.aap.api.config.Metrikker.Companion.ETTERSENDTE
import no.nav.aap.api.config.Metrikker.Companion.INKOMPLETT
import no.nav.aap.api.config.Metrikker.Companion.INNSENDTE
import no.nav.aap.api.config.Metrikker.Companion.KOMPLETT
import no.nav.aap.api.config.Metrikker.Companion.MANGLENDE
import no.nav.aap.api.config.Metrikker.Companion.SØKNADER
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.STANDARD_ETTERSENDING
import no.nav.aap.api.felles.SkjemaType.UTLAND_SØKNAD
import no.nav.aap.api.søknad.arkiv.ArkivClient.ArkivResultat
import no.nav.aap.api.søknad.fordeling.SøknadFordeler.Kvittering
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Companion.SISTE_SØKNAD
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.mellomlagring.Mellomlager
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.model.StandardEttersending
import no.nav.aap.api.søknad.model.StandardEttersending.EttersendtVedlegg
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Utbetalinger.AnnenStønadstype.UTLAND
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil
import no.nav.aap.util.MDCUtil.callIdAsUUID
import no.nav.aap.util.StringExtensions.decap
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SøknadFullfører(private val dokumentLager: Dokumentlager,
                      private val minside: MinSideClient,
                      private val repo: SøknadRepository,
                      private val mellomlager: Mellomlager,
                      private val metrikker: Metrikker) {

    private val log = getLogger(javaClass)

    fun fullfør(fnr: Fødselsnummer, søknad: UtlandSøknad, res: ArkivResultat) =
        minside.opprettBeskjed(fnr, "Vi har mottatt din ${UTLAND_SØKNAD.tittel.decap()}").run {
            metrikker.inc(SØKNADER,"type", UTLAND.name.lowercase())
            Kvittering(res.journalpostId)
        }

    @Transactional
    fun fullfør(fnr: Fødselsnummer, søknad: StandardSøknad, res: ArkivResultat) =
        with(res) {
            dokumentLager.slettDokumenter(søknad).run {
                mellomlager.slett()
                with(søknad.vedlegg()) {
                    if (this.manglende.isEmpty()) {
                        metrikker.inc(KOMPLETT, "type", STANDARD.name.lowercase())
                    }
                    else {
                        metrikker.inc(INKOMPLETT, "type", STANDARD.name.lowercase())
                    }
                    with(repo.save(Søknad(fnr.fnr, journalpostId))) {
                        registrerManglende(manglende)
                        registrerVedlagte(vedlagte)
                        oppdaterMinSide(fnr, manglende.isEmpty())
                    }
                    manglende.forEach{
                        metrikker.inc(MANGLENDE,"type",it.name.lowercase())
                    }
                    vedlagte.forEach{
                        metrikker.inc(INNSENDTE,"type",it.name.lowercase())
                    }
                }
                metrikker.inc(SØKNADER,"type", STANDARD.name.lowercase())
                Kvittering(journalpostId,søknad.innsendingTidspunkt, callIdAsUUID())
            }
        }

    @Transactional
    fun fullfør(fnr: Fødselsnummer, e: StandardEttersending, res: ArkivResultat) =
        with(res) {
            dokumentLager.slettDokumenter(e).run {
                e.søknadId?.let {
                    fullførEttersending(fnr, it, e.ettersendteVedlegg, this@with)
                } ?: fullførEttersendingUtenSøknad(fnr, e.ettersendteVedlegg, this@with)
                e.ettersendteVedlegg.forEach{
                    log.trace("Vedlagt vedlegg $it")
                    metrikker.inc(ETTERSENDTE,"type",it.vedleggType.name.lowercase())
                    metrikker.inc(INNSENDTE,"type",it.vedleggType.name.lowercase())
                }
                metrikker.inc(SØKNADER,"type", STANDARD_ETTERSENDING.name.lowercase())
                Kvittering(journalpostId)
            }
        }

    private fun fullførEttersending(fnr: Fødselsnummer,
                                    søknadId: UUID,
                                    e: List<EttersendtVedlegg>,
                                    res: ArkivResultat) =
        repo.getSøknadByEventidAndFnr(søknadId, fnr.fnr)?.let {
            it.registrerEttersending(fnr, res, e)
            it.avsluttMinSideOppgaveHvisKomplett(fnr)
            minside.opprettBeskjed(fnr, "Vi har mottatt din ${STANDARD_ETTERSENDING.tittel.decap()}.")
        } ?: log.warn("Ingen tidligere innsendt søknad med id $søknadId ble funnet for $fnr (dette skal aldri skje)")

    private fun fullførEttersendingUtenSøknad(fnr: Fødselsnummer, e: List<EttersendtVedlegg>, res: ArkivResultat) {
        repo.getSøknadByFnr(fnr.fnr, SISTE_SØKNAD).firstOrNull()?.let {
            log.info("Knytter ettersending til siste søknad ${it.eventid} med journalpost ${it.journalpostid}")
            it.registrerEttersending(fnr, res, e)
        } ?: log.trace("Fant ingen sist innsendt søknad for $fnr")
        minside.opprettBeskjed(fnr, "Vi har mottatt din ${STANDARD_ETTERSENDING.tittel.decap()}")
    }

    private fun Søknad.oppdaterMinSide(fnr: Fødselsnummer, erKomplett: Boolean) =
        if (!erKomplett) {
            log.trace("Oppretter oppgave og beskjed siden det er manglende vedlegg ${MDCUtil.callIdAsUUID()}")
            minside.opprettOppgave(fnr, "Vi har mottatt din ${STANDARD.tittel.decap()}. Du må ettersende dokumentasjon",
                   eventid)
            minside.opprettBeskjed(fnr, "Vi har mottatt din ${STANDARD.tittel.decap()}", eventId  = randomUUID())
        }
        else {
            minside.opprettBeskjed(fnr, "Vi har mottatt din ${STANDARD.tittel.decap()}", eventId  = eventid)
        }

    private fun Søknad.avsluttMinSideOppgaveHvisKomplett(fnr: Fødselsnummer) =
        with(manglendevedlegg) {
            if (isEmpty()) {
                log.info("Alle manglende vedlegg er sendt inn, avslutter oppgave $eventid")
                minside.avsluttOppgave(fnr, eventid)
            }
            else {
                log.trace("Det mangler fremdeles $size vedlegg (${map { it.vedleggtype }})")
            }
        }
}