package no.nav.aap.api.søknad.routing

import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.api.søknad.routing.standard.StandardSøknadRouter
import no.nav.aap.api.søknad.routing.utland.UtlandSøknadRouter
import no.nav.boot.conditionals.ConditionalOnGCP

@ConditionalOnGCP
class SøknadRouter(private val utland: UtlandSøknadRouter, private val standard: StandardSøknadRouter) : Router {
    override fun route(søknad: UtlandSøknad) = utland.route(søknad)
    override fun route(søknad: StandardSøknad) = standard.route(søknad)
}

interface Router {
    fun route(søknad: UtlandSøknad): Kvittering
    fun route(søknad: StandardSøknad): Kvittering
}