package no.nav.aap.api.oppslag

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdDTO.Arbeidsforhold
import no.nav.aap.api.oppslag.behandler.Behandler
import no.nav.aap.api.søknad.model.Søker

data class SøkerInfo(val søker: Søker?, val behandlere: List<Behandler>,val arbeidsforhold: List<Arbeidsforhold>)