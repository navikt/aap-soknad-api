package no.nav.aap.api.søknad.formidling.standard

import no.nav.aap.api.mellomlagring.DokumentLagerController.Companion.BASEPATH
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.dittnav.DittNavFormidler
import no.nav.aap.api.søknad.joark.JoarkFormidler
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardSøknad
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri

@Component
internal class StandardSøknadFormidler(private val joark: JoarkFormidler,
                                       private val pdl: PDLClient,
                                       private val dittnav: DittNavFormidler,
                                       private val vlRouter: VLRouter,
                                       private val vl: StandardSøknadVLFormidler) {

    fun formidle(søknad: StandardSøknad) =
        with(pdl.søkerMedBarn()) {
            val res = joark.formidle(søknad, this)
            if (vlRouter.skalTilVL(søknad)){
                vl.formidle(søknad, this, res.second)
            }
            dittnav.opprettBeskjed()
            Kvittering(fromCurrentRequestUri().replacePath("${BASEPATH}/les/${res.first}").build().toUri())
        }
}