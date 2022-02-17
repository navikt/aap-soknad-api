package no.nav.aap.api.oppslag

import no.nav.aap.api.oppslag.fastlege.Fastlege
import no.nav.aap.api.søknad.model.Søker

data class SøkerInfo(val søker: Søker?, val fastlege: Fastlege?)