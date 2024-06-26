package no.nav.aap.api.oppslag

import no.nav.aap.api.felles.Kontonummer
import no.nav.aap.api.oppslag.arbeid.Arbeidsforhold
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler
import no.nav.aap.api.oppslag.kontaktinformasjon.Kontaktinformasjon
import no.nav.aap.api.oppslag.person.Søker

data class SøkerInfo(val søker: Søker,
                     val behandlere: List<RegistrertBehandler>,
                     val arbeidsforhold: List<Arbeidsforhold>,
                     val kontaktinformasjon: Kontaktinformasjon?,
                     val konto: Kontonummer?)