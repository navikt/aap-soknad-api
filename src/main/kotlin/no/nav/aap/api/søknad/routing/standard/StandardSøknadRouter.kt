package no.nav.aap.api.søknad.routing.standard

import no.nav.aap.api.mellomlagring.DokumentLagerController.Companion.BASEPATH
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.SkjemaType
import no.nav.aap.api.søknad.dittnav.DittNavRouter
import no.nav.aap.api.søknad.joark.JoarkRouter
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.routing.VLRouter
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri

@Component
internal class StandardSøknadRouter(private val joark: JoarkRouter,
                                    private val pdl: PDLClient,
                                    private val dittnav: DittNavRouter,
                                    private val vlRouter: VLRouter,
                                    private val vl: StandardSøknadVLRouter) {

    fun route(søknad: StandardSøknad) =
        with(pdl.søkerMedBarn()) {
            val res = joark.route(søknad, this)
            if (vlRouter.skalTilVL(søknad)){
                vl.route(søknad, this, res.second)
            }
            dittnav.opprettBeskjed(SkjemaType.HOVED)
            Kvittering(fromCurrentRequestUri().replacePath("${BASEPATH}/les/${res.first}").build().toUri())
        }
}