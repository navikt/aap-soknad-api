package no.nav.aap.api.oppslag.arbeidsforhold

import org.springframework.stereotype.Component

@Component
class ArbeidClient(private val adapter: ArbeidClientAdapter, private val orgNavnAdapter: OrganisasjonWebClientAdapter)  {
    fun arbeidsforhold() =
         adapter.arbeidsforhold()
            ?.map { it.tilArbeidsforhold(orgNavnAdapter.orgNavn(it.arbeidsgiver.organisasjonsnummer)) }
            .orEmpty()
}