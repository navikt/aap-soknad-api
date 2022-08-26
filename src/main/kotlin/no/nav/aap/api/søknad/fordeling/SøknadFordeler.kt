package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.ettersendelse.Ettersending
import no.nav.aap.api.søknad.fordeling.StandardSøknadFordeler.UtlandSøknadFordeler
import no.nav.aap.api.søknad.fordeling.SøknadRepository.InnsendteVedlegg
import no.nav.aap.api.søknad.fordeling.SøknadRepository.ManglendeVedlegg
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.joark.JoarkFordeler
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
    override fun ettersend(ettersending: Ettersending) = standard.ettersend(ettersending)
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
    private val log = getLogger(javaClass)

    fun fordel(søknad: StandardSøknad) =
        pdl.søkerMedBarn().run {
            with(joark.fordel(søknad, this)) {
                vl.fordel(søknad, fnr, journalpostId, cfg.standard)
                fullfører.fullfør(søknad, this@run.fnr, this@with)
            }
        }

    fun ettersend(ettersending: Ettersending) {
        with(pdl.søkerUtenBarn()) {
            joark.ettersend(ettersending, this)
            fullfører.fullfør(ettersending, this.fnr)
        }
    }

    @Component
    class StandardSøknadFullfører(private val dokumentLager: Dokumentlager,
                                  private val dittnav: MinSideClient,
                                  private val repo: SøknadRepository,
                                  private val mellomlager: Mellomlager) {

        private val log = getLogger(javaClass)

        @Transactional
        fun fullfør(søknad: StandardSøknad, søker: Fødselsnummer, resultat: JoarkFordelingResultat) =
            dokumentLager.slettDokumenter(søknad).run {
                mellomlager.slett()
                val s =
                    repo.save(Søknad(fnr = søker.fnr, journalpostid = resultat.journalpostId, eventid = callIdAsUUID()))

                with(søknad.vedlegg()) {  //
                    log.trace("VedleggInfo $this")
                    manglende.forEach { type ->
                        with(ManglendeVedlegg(soknad = s, vedleggtype = type, eventid = s.eventid)) {
                            s.manglendevedlegg.add(this)
                            soknad = s
                        }
                    }
                    innsendte.forEach { type ->
                        with(InnsendteVedlegg(soknad = s, vedleggtype = type, eventid = s.eventid)) {
                            s.innsendtevedlegg.add(this)
                            soknad = s
                        }
                    }
                    if (manglende.isNotEmpty()) {
                        dittnav.opprettOppgave(MINAAPSTD, søker, s.eventid,
                                "Vi har mottatt din ${STANDARD.tittel}. Du må ettersende dokumentasjon")
                    }
                    else {
                        dittnav.opprettBeskjed(MINAAPSTD, s.eventid, søker,
                                "Vi har mottatt din ${STANDARD.tittel}", true)
                    }
                }
                Kvittering(dokumentLager.lagreDokument(DokumentInfo(bytes = resultat.pdf, navn = "kvittering.pdf")))
            }

        @Transactional
        fun fullfør(ettersending: Ettersending, fnr: Fødselsnummer) {
            repo.getSøknadByEventidAndFnr(ettersending.søknadId, fnr.fnr)?.let { s ->
                s.manglendevedlegg?.forEach {
                    ettersending.ettersendteVedlegg.forEach { ev ->
                        if (ev.type == it.vedleggtype) {
                            log.trace("Manglende søknad $it ble nå sendt inn")
                            s.manglendevedlegg.remove(it)  // TODO merke istedet for å slette?
                            with(InnsendteVedlegg(soknad = s, vedleggtype = ev.type, eventid = s.eventid)) {
                                s.innsendtevedlegg.add(this)
                                soknad = s
                            }
                        }
                    }
                }
            } ?: log.warn("Ingen tidligere innsendt søknad med ud ${ettersending.søknadId} ble funnet")
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