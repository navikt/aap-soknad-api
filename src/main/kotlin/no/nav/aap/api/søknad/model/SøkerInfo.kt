package no.nav.aap.api.søknad.model

import no.nav.aap.api.oppslag.arbeid.Arbeidsforhold
import no.nav.aap.api.oppslag.behandler.Behandler
import no.nav.aap.api.oppslag.krr.KontaktinformasjonDTO

data class SøkerInfo(val søker: Søker?,
                     val behandlere: List<Behandler>,
                     val arbeidsforhold: List<Arbeidsforhold>,
                     val kontaktinformasjon: KontaktinformasjonDTO?)