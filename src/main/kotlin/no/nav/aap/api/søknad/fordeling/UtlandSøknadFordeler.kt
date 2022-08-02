package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.joark.JoarkFordeler
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.UtlandSøknad
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.stereotype.Component
import java.util.*

@Component
class UtlandSøknadFordeler(private val joark: JoarkFordeler,
                           private val pdl: PDLClient,
                           private val dittnav: DittNavClient,
                           private val lager: Dokumentlager,
                           private val cfg: VLFordelingConfig,
                           private val vl: SøknadVLFordeler) {

    fun fordel(søknad: UtlandSøknad) =
        with(pdl.søkerUtenBarn()) outer@{
            with(joark.fordel(søknad, this)) {
                vl.fordel(søknad, this@outer.fnr, journalpostId, cfg.utland)
                dittnav.opprettBeskjed(UTLAND, UUID.randomUUID(), fnr, "Vi har mottatt ${UTLAND.tittel}")
                Kvittering(lager.lagreDokument(DokumentInfo(pdf, APPLICATION_PDF_VALUE, "kvittering.pdf")))
            }
        }
}