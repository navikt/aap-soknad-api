package no.nav.aap.api.søknad.routing.utland

import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.joark.JoarkRouter
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.AuthContext
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.stereotype.Component

@Component
class UtlandSøknadRouter(private val joark: JoarkRouter,
                         private val pdl: PDLClient,
                         private val dittnav: DittNavClient,
                         private val ctx: AuthContext,
                         private val lager: Dokumentlager,
                         private val router: UtlandSøknadVLRouter) {

    fun route(søknad: UtlandSøknad) =
        with(pdl.søkerUtenBarn()) outer@{
            with(joark.route(søknad, this)) {
                router.route(søknad, this@outer, second)
                dittnav.opprettBeskjed(UTLAND)
                Kvittering(lager.lagreDokument(ctx.getFnr(),
                        DokumentInfo(first, APPLICATION_PDF_VALUE, "kvittering.pdf")))
            }
        }
}