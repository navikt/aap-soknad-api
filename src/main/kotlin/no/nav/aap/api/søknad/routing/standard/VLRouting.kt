package no.nav.aap.api.søknad.routing.standard

import no.nav.aap.api.søknad.model.StandardSøknad
import org.springframework.stereotype.Component

@Component
class BlockingVLRouter :VLRouter {
    override fun skalTilVL(søknad: StandardSøknad) = false
}

interface VLRouter {
    fun  skalTilVL(søknad: StandardSøknad) : Boolean
}