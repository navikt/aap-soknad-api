package no.nav.aap.api.søknad.routing.standard

import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.joark.JoarkRouter
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.routing.VLRouter
import no.nav.aap.util.AuthContext
import org.springframework.stereotype.Component

@Component
class StandardSøknadRouter(private val joark: JoarkRouter,
                           private val pdl: PDLClient,
                           private val dittnav: DittNavClient,
                           private val lager: Dokumentlager,
                           private val vlRouter: VLRouter,
                           private val ctx: AuthContext,
                           private val vl: StandardSøknadVLRouter) {

    fun route(søknad: StandardSøknad) =
        with(pdl.søkerMedBarn()) outer@{
            with(joark.route(søknad, this)) {
                if (vlRouter.shouldRoute(søknad)) {
                    vl.route(søknad, this@outer, second.journalpostId)
                }
                lager.slettDokumenter(ctx.getFnr(), søknad)
                dittnav.exit()
                Kvittering("$first")
            }
        }
}