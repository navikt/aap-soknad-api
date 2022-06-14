package no.nav.aap.api.søknad.routing.utland

import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.joark.JoarkRouter
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.api.søknad.routing.UtlandSøknadVLRouter
import no.nav.aap.api.søknad.routing.VLRouter
import org.springframework.stereotype.Component

@Component
class UtlandSøknadRouter(private val joark: JoarkRouter,
                         private val pdl: PDLClient,
                         private val vlRouter: VLRouter,
                         private val dittnav: DittNavClient,
                         private val router: UtlandSøknadVLRouter) {

    fun route(søknad: UtlandSøknad) =
        with(pdl.søkerUtenBarn()) outer@{
            with(joark.route(søknad, this)) {
                if (vlRouter.shouldRoute(søknad)) {
                    router.route(søknad, this@outer, second)
                }
                dittnav.opprettBeskjed(UTLAND)
                Kvittering("$first")
            }
        }
}