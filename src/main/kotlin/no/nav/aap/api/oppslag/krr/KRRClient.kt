package no.nav.aap.api.oppslag.krr

import org.springframework.stereotype.Component

@Component
class KRRClient(private val adapter: KRRWebClientAdapter) {
    fun kontaktinfo() = adapter.kontaktInformasjon()
}