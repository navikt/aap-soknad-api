package no.nav.aap.api.oppslag.kontaktinformasjon

import org.springframework.stereotype.Component

@Component
class KRRClient(private val adapter: KRRWebClientAdapter) {
    fun kontaktInfo() = adapter.kontaktInformasjon()
}