package no.nav.aap.api.oppslag.arbeid

import no.nav.aap.api.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("${ARBEID}.enabled", havingValue = "true")
class ArbeidClient(private val arbeid: ArbeidWebClientAdapter,
                   private val org: OrganisasjonWebClientAdapter) {
    fun arbeidInfo() =
        arbeid.arbeidInfo()
            .map {
                it.tilArbeidInfo(org.orgNavn(it.arbeidsgiver.organisasjonsnummer))
            }
}