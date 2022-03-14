package no.nav.aap.api.oppslag

import no.nav.aap.api.oppslag.behandler.Behandler
import no.nav.aap.api.søknad.model.Søker

data class SøkerInfo(val søker: Søker?, val behandler: Behandler?)