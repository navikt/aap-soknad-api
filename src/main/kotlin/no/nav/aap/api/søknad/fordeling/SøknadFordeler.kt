package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.joark.JoarkFordeler
import no.nav.aap.api.søknad.mellomlagring.Mellomlager
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
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
                             private val dittnav: DittNavClient,
                             private val avslutter: StandardSøknadAvslutter,
                             private val cfg: VLFordelingConfig,
                             private val repo: SøknadRepository,
                             private val vl: SøknadVLFordeler) {

    private val log = LoggerUtil.getLogger(javaClass)

    fun fordel(søknad: StandardSøknad) =
        pdl.søkerMedBarn().run {
            with(joark.fordel(søknad, this)) {
                vl.fordel(søknad, fnr, journalpostId, cfg.standard)
                dittnav.opprettBeskjed(fnr = fnr, tekst = "Vi har mottatt ${STANDARD.tittel}")
                    ?.let { uuid ->
                        log.info(CONFIDENTIAL, "Lagrer DB søknad med uuid $uuid $søknad")
                        repo.save(JPASøknad(fnr = this@run.fnr.fnr, soknad = søknad, eventid = uuid)).also {
                            log.info(CONFIDENTIAL, "Lagret DB søknad $it OK")
                        }
                    }
                log.trace("Manglende vedlegg er ${søknad.manglendeVedlegg()}")
                avslutter.avsluttSøknad(søknad, pdf)
            }
        }
}

@Component
class StandardSøknadAvslutter(private val dokumentLager: Dokumentlager,
                              private val mellomlager: Mellomlager) {
    fun avsluttSøknad(søknad: StandardSøknad, pdf: ByteArray) =
        dokumentLager.slettDokumenter(søknad).run {
            mellomlager.slett()
            Kvittering(dokumentLager.lagreDokument(DokumentInfo(pdf, APPLICATION_PDF_VALUE, "kvittering.pdf")))
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
                dittnav.opprettBeskjed(UTLAND, fnr = fnr, tekst = "Vi har mottatt ${UTLAND.tittel}")
                Kvittering(lager.lagreDokument(DokumentInfo(pdf, APPLICATION_PDF_VALUE, "kvittering-utland.pdf")))
            }
        }
}