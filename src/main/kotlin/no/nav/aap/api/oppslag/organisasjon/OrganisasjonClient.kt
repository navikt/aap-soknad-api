package no.nav.aap.api.oppslag.organisasjon

import org.springframework.stereotype.Component

@Component
class OrganisasjonClient(private val adapter: OrganisasjonWebClientAdapter) {
    fun orgNavn(orgnr: String?) = adapter.orgNavn(orgnr)
}