package no.nav.aap.api.oppslag.arbeidsforhold

import org.springframework.stereotype.Component

@Component
class ArbeidsforholdClient(private val adapter: ArbeidsforholdClientAdapter)  {
    fun arbeidsforhold() = adapter.arbeidsforhold()
}