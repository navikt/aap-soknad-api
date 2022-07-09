package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.søknad.fordeling.standard.StandardSøknaFordeler
import no.nav.aap.api.søknad.fordeling.utland.UtlandSøknadFordeler
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.boot.conditionals.ConditionalOnGCP

@ConditionalOnGCP
class SøknadFordeler(private val utland: UtlandSøknadFordeler, private val standard: StandardSøknaFordeler) :
    Router {
    override fun fordel(søknad: UtlandSøknad) = utland.fordel(søknad)
    override fun fordel(søknad: StandardSøknad) = standard.fordel(søknad)
}

interface Router {
    fun fordel(søknad: UtlandSøknad): Kvittering
    fun fordel(søknad: StandardSøknad): Kvittering
}