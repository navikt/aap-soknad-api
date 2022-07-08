package no.nav.aap.api.søknad.routing

import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.api.søknad.routing.standard.StandardSøknadLeverandør
import no.nav.aap.api.søknad.routing.utland.UtlandSøknadLeverandør
import no.nav.boot.conditionals.ConditionalOnGCP

@ConditionalOnGCP
class SøknadLeverandør(private val utland: UtlandSøknadLeverandør, private val standard: StandardSøknadLeverandør) :
    Router {
    override fun leverSøknad(søknad: UtlandSøknad) = utland.leverSøknad(søknad)
    override fun leverSøknad(søknad: StandardSøknad) = standard.leverSøknad(søknad)
}

interface Router {
    fun leverSøknad(søknad: UtlandSøknad): Kvittering
    fun leverSøknad(søknad: StandardSøknad): Kvittering
}