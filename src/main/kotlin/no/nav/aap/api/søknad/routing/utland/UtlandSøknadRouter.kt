package no.nav.aap.api.søknad.routing.utland

import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.joark.JoarkRouter
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.UtlandSøknad
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.stereotype.Component

@Component
class UtlandSøknadRouter(private val joarkRouter: JoarkRouter,
                         private val pdl: PDLClient,
                         private val dittnav: DittNavClient,
                         private val lager: Dokumentlager,
                         private val vlRouter: UtlandSøknadVLRouter) {

    fun route(søknad: UtlandSøknad) =
        with(pdl.søkerUtenBarn()) outer@{
            with(joarkRouter.route(søknad, this)) {
                vlRouter.route(søknad, this@outer, journalpostId)
                dittnav.opprettBeskjed(UTLAND)
                Kvittering(lager.lagreDokument(DokumentInfo(pdf, APPLICATION_PDF_VALUE, "kvittering.pdf")))
            }
        }
}