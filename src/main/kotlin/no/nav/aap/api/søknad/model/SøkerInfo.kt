package no.nav.aap.api.søknad.model

import no.nav.aap.api.oppslag.arbeid.Arbeidsforhold
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler
import no.nav.aap.api.oppslag.konto.KontoWebClientAdapter.Konto
import no.nav.aap.api.oppslag.krr.Kontaktinformasjon

data class SøkerInfo(val søker: Søker?,
                     val behandlere: List<RegistrertBehandler>,
                     val arbeidsforhold: List<Arbeidsforhold>,
                     val kontaktinformasjon: Kontaktinformasjon?,
                     val konto: Konto? = null)