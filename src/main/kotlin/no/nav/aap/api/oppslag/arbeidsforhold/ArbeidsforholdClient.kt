package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.api.oppslag.arbeidsforhold.OrganisasjonWebClientAdapter
import org.springframework.stereotype.Component

@Component
class ArbeidsforholdClient(private val adapter: ArbeidsforholdClientAdapter, private val orgNavnAdapter: OrganisasjonWebClientAdapter)  {
    fun arbeidsforhold() =
         adapter.arbeidsforhold()
            ?.map { it.tilArbeidsforhold(orgNavnAdapter.orgNavn(it.arbeidsgiver.organisasjonsnummer)) }
            .orEmpty()
}