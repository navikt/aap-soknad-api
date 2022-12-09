package no.nav.aap.api.oppslag.arbeid

import org.springframework.stereotype.Component

@Component
class ArbeidClient(private val arbeid: ArbeidWebClientAdapter,
                   private val org: OrganisasjonWebClientAdapter) {
    fun arbeidInfo() = arbeid.arbeidInfo().map { it.tilArbeidInfo(org.orgNavn(it.arbeidsgiver.organisasjonsnummer)) }
}