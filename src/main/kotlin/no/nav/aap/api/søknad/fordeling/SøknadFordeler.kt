package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.arkiv.ArkivFordeler
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardEttersending
import no.nav.aap.api.søknad.model.Innsending
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

interface Fordeler {
    fun fordel(søknad: UtlandSøknad): Kvittering
    fun fordel(innsending: Innsending): Kvittering
    fun fordel(ettersending: StandardEttersending): Kvittering

}

@Component
class SøknadFordeler(private val arkiv: ArkivFordeler,
                     private val pdl: PDLClient,
                     private val fullfører: SøknadFullfører,
                     private val cfg: VLFordelingConfig,
                     private val vl: SøknadVLFordeler) : Fordeler {
    private val log = getLogger(javaClass)

    override fun fordel(innsending: Innsending) =
        pdl.søkerMedBarn().run {
            log.trace("Fordeler $innsending")
            with(arkiv.fordel(innsending, this)) {
                vl.fordel(innsending.søknad, fnr, journalpostId, cfg.standard)
                fullfører.fullfør(innsending.søknad, this@run.fnr, this)
            }
        }

    override fun fordel(e: StandardEttersending) =
        pdl.søkerUtenBarn().run {
            log.trace("Fordeler $e")
            with(arkiv.fordel(e, this)) {
                vl.fordel(e, fnr, journalpostId, cfg.ettersending)
                fullfører.fullfør(e, this@run.fnr, this)
            }
        }

    override fun fordel(søknad: UtlandSøknad) =
        pdl.søkerUtenBarn().run {
            log.trace("Fordeler $søknad")
            with(arkiv.fordel(søknad, this)) {
                vl.fordel(søknad, fnr, journalpostId, cfg.utland)
                fullfører.fullfør(søknad, this@run.fnr, this)
            }
        }
}