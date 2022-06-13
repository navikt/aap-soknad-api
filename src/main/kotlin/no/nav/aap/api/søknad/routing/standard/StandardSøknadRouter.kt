package no.nav.aap.api.søknad.routing.standard

import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.joark.JoarkRouter
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.routing.VLRouter
import org.springframework.stereotype.Component

@Component
class StandardSøknadRouter(private val joark: JoarkRouter,
                           private val pdl: PDLClient,
                           private val dittnav: DittNavClient,
                           private val vlRouter: VLRouter,
                           private val vl: StandardSøknadVLRouter) {

    fun route(søknad: StandardSøknad) =
        with(pdl.søkerMedBarn()) outer@{
            with(joark.route(søknad, this)) {
                if (vlRouter.shouldRoute(søknad)) {
                    vl.route(søknad, this@outer, second.journalpostId)
                }
                dittnav.opprettBeskjed(STANDARD)
                Kvittering("$first")
            }
        }
}