package no.nav.aap.api.søknad.formidling

import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker

internal  interface SøknadFormidler<T> {

    fun formidle(søknad: StandardSøknad, søker: Søker): T
}