package no.nav.aap.api.oppslag.arbeidsforhold

import org.springframework.stereotype.Component

@Component
class ArbeidsforholdClient(private val adapter: ArbeidsforholdWebClientAdapter)  {
    fun arbeidsforhold() = adapter.arbeidsforhold()
}