package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.arkiv.ArkivFordeler
import no.nav.aap.api.søknad.arkiv.ArkivFordeler.ArkivEttersendingResultat
import no.nav.aap.api.søknad.arkiv.ArkivFordeler.ArkivSøknadResultat
import no.nav.aap.api.søknad.fordeling.StandardSøknadFordeler.UtlandSøknadFordeler
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Ettersending
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.mellomlagring.Mellomlager
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.MINAAPSTD
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.MINAAPUTLAND
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardEttersending
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
    override fun fordel(ettersending: StandardEttersending) = standard.fordel(ettersending)
}

interface Fordeler {
    fun fordel(søknad: UtlandSøknad): Kvittering
    fun fordel(søknad: StandardSøknad): Kvittering
    fun fordel(ettersending: StandardEttersending)

}

@Component
class StandardSøknadFordeler(private val arkiv: ArkivFordeler,
                             private val pdl: PDLClient,
                             private val fullfører: StandardSøknadFullfører,
                             private val cfg: VLFordelingConfig,
                             private val vl: SøknadVLFordeler) {

    fun fordel(søknad: StandardSøknad) =
        pdl.søkerMedBarn().run {
            with(arkiv.fordel(søknad, this)) {
                vl.fordel(søknad, fnr, journalpostId, cfg.standard)
                fullfører.fullfør(søknad, this@run.fnr, this)
            }
        }

    fun fordel(e: StandardEttersending) =
        pdl.søkerUtenBarn().run {
            with(arkiv.fordel(e, this)) {
                vl.fordel(e, fnr, journalpostId, cfg.ettersending)
                fullfører.fullfør(e, this@run.fnr, this)
            }
        }

    @Component
    class StandardSøknadFullfører(private val dokumentLager: Dokumentlager,
                                  private val minside: MinSideClient,
                                  private val søknader: SøknadRepository,
                                  private val mellomlager: Mellomlager) {

        private val log = getLogger(javaClass)

        @Transactional
        fun fullfør(søknad: StandardSøknad, fnr: Fødselsnummer, res: ArkivSøknadResultat) =
            dokumentLager.slettDokumenter(søknad).run {
                mellomlager.slett()
                with(søknad.vedlegg()) {
                    with(søknader.save(Søknad(fnr.fnr, res.journalpostId, callIdAsUUID()))) {
                        registrerSomManglende(manglende)
                        registrerSomVedlagte(vedlagte)
                        oppdaterMinSide(manglende.isEmpty(), fnr)
                    }
                }
                Kvittering(dokumentLager.lagreDokument(DokumentInfo(res.pdf, "kvittering.pdf")))
            }

        @Transactional
        fun fullfør(e: StandardEttersending, fnr: Fødselsnummer, res: ArkivEttersendingResultat) =
            dokumentLager.slettDokumenter(e).run {
                søknader.getSøknadByEventidAndFnr(e.søknadId, fnr.fnr)?.let {
                    with(it) {
                        val es = Ettersending(fnr.fnr, res.journalpostId, callIdAsUUID(), it)
                        it.ettersendinger.add(es)
                        tidligereManglendeNåEttersendte(e.ettersendteVedlegg)
                            .forEach(::registrerVedlagtFraEttersending)
                        avsluttMinSideOppgaveHvisKomplett(fnr)
                        // TODO lagre og returnere kvittering
                    }
                } ?: log.warn("Ingen tidligere innsendt søknad med id ${e.søknadId} ble funnet for $fnr")
            }

        private fun Søknad.oppdaterMinSide(erKomplett: Boolean, fnr: Fødselsnummer) =
            if (erKomplett) {
                minside.opprettBeskjed(MINAAPSTD, eventid, fnr,
                        "Vi har mottatt din ${STANDARD.tittel}", true)
            }
            else {
                minside.opprettOppgave(MINAAPSTD, fnr, eventid,
                        "Vi har mottatt din ${STANDARD.tittel}. Du må ettersende dokumentasjon")
            }

        private fun Søknad.avsluttMinSideOppgaveHvisKomplett(fnr: Fødselsnummer) {
            if (manglendevedlegg.isEmpty()) {
                minside.avsluttOppgave(STANDARD, fnr, eventid).also {
                    log.trace("Alle manglende vedlegg er sendt inn, avsluttet oppgave $eventid")
                }
            }
            else {
                with(manglendevedlegg) {
                    log.trace("Det mangler fremdeles $size vedlegg (${map { it.vedleggtype }})")
                }
            }
        }

    }

    @Component
    class UtlandSøknadFordeler(private val arkiv: ArkivFordeler,
                               private val pdl: PDLClient,
                               private val dittnav: MinSideClient,
                               private val lager: Dokumentlager,
                               private val cfg: VLFordelingConfig,
                               private val vl: SøknadVLFordeler) {

        fun fordel(søknad: UtlandSøknad) =
            pdl.søkerUtenBarn().run {
                with(arkiv.fordel(søknad, this)) {
                    vl.fordel(søknad, fnr, journalpostId, cfg.utland)
                    dittnav.opprettBeskjed(MINAAPUTLAND, callIdAsUUID(), fnr, "Vi har mottatt ${UTLAND.tittel}", true)
                    Kvittering(lager.lagreDokument(DokumentInfo(pdf, "kvittering-utland.pdf")))
                }
            }
    }
}