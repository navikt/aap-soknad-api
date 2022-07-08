package no.nav.aap.api.søknad.routing.utland

import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.joark.JoarkLeverandør
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.UtlandSøknad
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.stereotype.Component

@Component
class UtlandSøknadLeverandør(private val joarkRouter: JoarkLeverandør,
                             private val pdl: PDLClient,
                             private val dittnav: DittNavClient,
                             private val lager: Dokumentlager,
                             private val vlRouter: UtlandSøknadVLLeverandør) {

    fun leverSøknad(søknad: UtlandSøknad) =
        with(pdl.søkerUtenBarn()) outer@{
            with(joarkRouter.leverSøknad(søknad, this)) {
                vlRouter.route(søknad, this@outer, journalpostId)
                //dittnav.opprettBeskjed(UTLAND, tekst = "Vi har mottatt en søknad om AAP (utland)")
                Kvittering(lager.lagreDokument(DokumentInfo(pdf, APPLICATION_PDF_VALUE, "kvittering.pdf")))
            }
        }
}