package no.nav.aap.api.søknad.routing.utland

import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.dittnav.DittNavRouter
import no.nav.aap.api.søknad.joark.JoarkRouter
import no.nav.aap.api.søknad.joark.pdf.PDFGeneratorWebClientAdapter.UtlandData
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.SkjemaType.UTLAND
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.api.søknad.routing.UtlandSøknadVLRouter
import no.nav.aap.api.søknad.routing.VLRouter
import org.springframework.stereotype.Component

@Component
class UtlandSøknadRouter(private val joark: JoarkRouter,
                         private val pdl: PDLClient,
                         private val vlRouter: VLRouter,
                         private val dittnav: DittNavRouter,
                         private val router: UtlandSøknadVLRouter) {

    fun route(søknad: UtlandSøknad) =
        with(pdl.søkerUtenBarn()) {
            val uuid = joark.route(søknad,this).first
            if (vlRouter.shouldRoute(søknad)) {
                router.route(UtlandData(this,søknad))
            }
            dittnav.opprettBeskjed(this.fødselsnummer,UTLAND)
            Kvittering("$uuid")
        }
}