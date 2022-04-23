package no.nav.aap.api.søknad.routing

import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import org.springframework.stereotype.Component

@Component
class BlockingVLRouter :VLRouter {
    override fun shouldRoute(søknad: StandardSøknad) = false
    override fun shouldRoute(søknad: UtlandSøknad) = false

}

interface VLRouter {
    fun  shouldRoute(søknad: StandardSøknad) : Boolean
     fun shouldRoute(søknad: UtlandSøknad) : Boolean
}