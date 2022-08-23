package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavNotifikasjonType.Companion.MINAAPSTD
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavNotifikasjonType.Companion.MINAAPUTLAND
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.fordeling.VedleggRepository.ManglendeVedlegg
import no.nav.aap.api.søknad.joark.JoarkFordeler
import no.nav.aap.api.søknad.joark.JoarkFordeler.FordelingResultat
import no.nav.aap.api.søknad.mellomlagring.Mellomlager
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.callIdAsUUID
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.stereotype.Component

@ConditionalOnGCP
class SøknadFordeler(private val utland: UtlandSøknadFordeler, private val standard: StandardSøknadFordeler) :
    Fordeler {
    override fun fordel(søknad: UtlandSøknad) = utland.fordel(søknad)
    override fun fordel(søknad: StandardSøknad) = standard.fordel(søknad)
}

interface Fordeler {
    fun fordel(søknad: UtlandSøknad): Kvittering
    fun fordel(søknad: StandardSøknad): Kvittering
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
                fullfører.fullfør(søknad, this@run, this@with)
            }
        }
}

@Component
class StandardSøknadFullfører(private val dokumentLager: Dokumentlager,
                              private val dittnav: DittNavClient,
                              private val repo: SøknadRepository,
                              private val mellomlager: Mellomlager) {

    private val log = getLogger(javaClass)

    fun fullfør(søknad: StandardSøknad, søker: Søker, resultat: FordelingResultat) =
        dokumentLager.slettDokumenter(søknad).run {
            mellomlager.slett()
            log.trace(CONFIDENTIAL, "Lagrer metadata om søknad i DB")
            val s =
                with(Søknad(fnr = søker.fnr.fnr, journalpostid = resultat.journalpostId, eventid = callIdAsUUID())) {
                    repo.save(this).also {
                        log.trace("Lagret metadata om søknad i DB OK")
                    }
                }
            if (søknad.manglendeVedlegg().isNotEmpty()) {
                dittnav.opprettOppgave(MINAAPSTD,
                        søker.fnr,
                        s.eventid,
                        "Vi har mottatt din ${STANDARD.tittel}. Du må ettersende dokumentasjon")?.let { eventId ->
                    søknad.manglendeVedlegg().forEach { type ->
                        with(ManglendeVedlegg(soknad = s,
                                vedleggtype = type,
                                eventid = eventId)) {
                            s.manglendevedlegg.add(this)
                            soknad = s
                        }
                    }
                    repo.save(s).also {
                        log.trace("Oppdatert metadata om søknad med ${s.manglendevedlegg.size} manglende vedlegg i DB OK")
                    }
                }
            }
            else {
                dittnav.opprettBeskjed(MINAAPSTD, callIdAsUUID(), søker.fnr, "Vi har mottatt din ${STANDARD.tittel}")
            }
            Kvittering(dokumentLager.lagreDokument(DokumentInfo(bytes = resultat.pdf, navn = "kvittering.pdf")))
        }
}

@Component
class UtlandSøknadFordeler(private val joark: JoarkFordeler,
                           private val pdl: PDLClient,
                           private val dittnav: DittNavClient,
                           private val lager: Dokumentlager,
                           private val cfg: VLFordelingConfig,
                           private val vl: SøknadVLFordeler) {

    fun fordel(søknad: UtlandSøknad) =
        pdl.søkerUtenBarn().run {
            with(joark.fordel(søknad, this)) {
                vl.fordel(søknad, fnr, journalpostId, cfg.utland)
                dittnav.opprettBeskjed(MINAAPUTLAND, callIdAsUUID(), fnr, "Vi har mottatt ${UTLAND.tittel}")
                Kvittering(lager.lagreDokument(DokumentInfo(pdf, navn = "kvittering-utland.pdf")))
            }
        }
}