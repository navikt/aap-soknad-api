package no.nav.aap.api.søknad.formidling

import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker

interface SøknadFormidler<T> {

    fun  formidle(søker: Søker, søknad: StandardSøknad) : T
}