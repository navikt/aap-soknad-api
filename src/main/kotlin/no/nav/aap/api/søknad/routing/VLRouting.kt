package no.nav.aap.api.søknad.routing

import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import org.springframework.stereotype.Component

@Component
class BlockingVLRouter :VLRouter {
    override fun skalTilVL(søknad: StandardSøknad) = false
    override fun skalTilVL(søknad: UtlandSøknad) = false

}

interface VLRouter {
    fun  skalTilVL(søknad: StandardSøknad) : Boolean
     fun skalTilVL(søknad: UtlandSøknad) : Boolean
}