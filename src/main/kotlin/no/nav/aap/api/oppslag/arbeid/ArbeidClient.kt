package no.nav.aap.api.oppslag.arbeid

import org.springframework.stereotype.Component

@Component
class ArbeidClient(private val arbeid: ArbeidWebClientAdapter,
                   private val org: OrganisasjonWebClientAdapter) {
    fun arbeidsforhold() =
        arbeid.arbeidsforhold()
            ?.map {
                it.tilArbeidsforhold(org.orgNavn(it.arbeidsgiver.organisasjonsnummer))
            }
}