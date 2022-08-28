package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.ettersendelse.Ettersending
import no.nav.aap.api.søknad.fordeling.StandardSøknadFordeler.UtlandSøknadFordeler
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.joark.JoarkFordeler
import no.nav.aap.api.søknad.joark.JoarkFordeler.JoarkEttersendingResultat
import no.nav.aap.api.søknad.joark.JoarkFordeler.JoarkFordelingResultat
import no.nav.aap.api.søknad.mellomlagring.Mellomlager
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.MINAAPSTD
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.MINAAPUTLAND
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.callIdAsUUID
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@ConditionalOnGCP
class SøknadFordeler(private val utland: UtlandSøknadFordeler, private val standard: StandardSøknadFordeler) :
    Fordeler {
    override fun fordel(søknad: UtlandSøknad) = utland.fordel(søknad)
    override fun fordel(søknad: StandardSøknad) = standard.fordel(søknad)
    override fun ettersend(ettersending: Ettersending) = standard.fordel(ettersending)
}

interface Fordeler {
    fun fordel(søknad: UtlandSøknad): Kvittering
    fun fordel(søknad: StandardSøknad): Kvittering

    fun ettersend(ettersending: Ettersending)

}

@Component
class StandardSøknadFordeler(private val joark: JoarkFordeler,
                             private val pdl: PDLClient,
                             private val fullfører: StandardSøknadFullfører,
                             private val cfg: VLFordelingConfig,
                             private val vl: SøknadVLFordeler) {

    fun fordel(søknad: StandardSøknad) =
        pdl.søkerMedBarn().run {
            with(joark.fordel(søknad, this)) {
                vl.fordel(søknad, fnr, journalpostId, cfg.standard)
                fullfører.fullfør(søknad, this@run.fnr, this@with)
            }
        }

    fun fordel(e: Ettersending) =
        pdl.søkerUtenBarn().run {
            with(joark.fordel(e, this)) {
                // TODO fordel til VL
                fullfører.fullfør(e, this@run.fnr, this)
            }
        }

    @Component
    class StandardSøknadFullfører(private val dokumentLager: Dokumentlager,
                                  private val minside: MinSideClient,
                                  private val repo: SøknadRepository,
                                  private val mellomlager: Mellomlager) {

        private val log = getLogger(javaClass)

        @Transactional
        fun fullfør(søknad: StandardSøknad, fnr: Fødselsnummer, res: JoarkFordelingResultat) =
            dokumentLager.slettDokumenter(søknad).run {
                mellomlager.slett()
                with(søknad.vedlegg()) {
                    with(repo.save(Søknad(fnr = fnr.fnr,
                            journalpostid = res.journalpostId,
                            eventid = callIdAsUUID()))) {
                        registrerSomManglende(manglende)
                        registrerSomVedlagte(vedlagte)
                        oppdaterMinSide(manglende.isEmpty())
                    }
                }
                Kvittering(dokumentLager.lagreDokument(DokumentInfo(bytes = res.pdf, navn = "kvittering.pdf")))
            }

        @Transactional
        fun fullfør(e: Ettersending, fnr: Fødselsnummer, resultat: JoarkEttersendingResultat) =
            dokumentLager.slettDokumenter(e).run {
                repo.getSøknadByEventidAndFnr(e.søknadId, fnr.fnr)?.let {
                    with(it) {
                        tidligereManglendeNåVedlagte(e.ettersendteVedlegg).forEach { m ->
                            registrerVedlagtFraEttersending(m)
                        }
                        avsluttMinSideOppgaveHvisKomplett()
                        // TODO lagre og returnere kvittering
                    }
                } ?: log.warn("Ingen tidligere innsendt søknad med søknadId ${e.søknadId} ble funnet for $fnr")
            }

        private fun Søknad.oppdaterMinSide(erKomplett: Boolean) =
            if (erKomplett) {
                minside.opprettBeskjed(MINAAPSTD, eventid, Fødselsnummer(fnr),
                        "Vi har mottatt din ${STANDARD.tittel}", true)
            }
            else {
                minside.opprettOppgave(MINAAPSTD, Fødselsnummer(fnr), eventid,
                        "Vi har mottatt din ${STANDARD.tittel}. Du må ettersende dokumentasjon")
            }

        private fun Søknad.avsluttMinSideOppgaveHvisKomplett() {
            with(manglendevedlegg) {
                if (isEmpty()) {
                    log.trace("Alle manglende vedlegg er sendt inn, avslutter oppgave $eventid")
                    minside.avsluttOppgave(STANDARD, Fødselsnummer(fnr), eventid)
                }
                else {
                    log.trace("Det mangler fremdeles $size vedlegg (${map { it.vedleggtype }})")
                }
            }
        }

    }

    @Component
    class UtlandSøknadFordeler(private val joark: JoarkFordeler,
                               private val pdl: PDLClient,
                               private val dittnav: MinSideClient,
                               private val lager: Dokumentlager,
                               private val cfg: VLFordelingConfig,
                               private val vl: SøknadVLFordeler) {

        fun fordel(søknad: UtlandSøknad) =
            pdl.søkerUtenBarn().run {
                with(joark.fordel(søknad, this)) {
                    vl.fordel(søknad, fnr, journalpostId, cfg.utland)
                    dittnav.opprettBeskjed(MINAAPUTLAND, callIdAsUUID(), fnr, "Vi har mottatt ${UTLAND.tittel}", true)
                    Kvittering(lager.lagreDokument(DokumentInfo(pdf, navn = "kvittering-utland.pdf")))
                }
            }
    }
}