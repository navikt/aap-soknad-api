package no.nav.aap.api.oppslag.fastlege

import no.nav.aap.api.oppslag.pdl.PDLOperations
import org.springframework.stereotype.Component

@Component
class FastlegeClient(private val adapter: FastlegeClientAdapter) {
    fun fastlege() = adapter.fastlege()
}