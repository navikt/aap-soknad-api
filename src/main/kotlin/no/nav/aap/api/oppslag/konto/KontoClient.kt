package no.nav.aap.api.oppslag.konto

import org.springframework.stereotype.Component

@Component
class KontoClient(private val adapter: KontoWebClientAdapter) {
    fun kontoInfo() = adapter.kontoInfo()
}