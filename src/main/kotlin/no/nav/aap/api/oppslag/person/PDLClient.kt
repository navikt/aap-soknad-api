package no.nav.aap.api.oppslag.person

import io.github.resilience4j.retry.annotation.Retry
import io.micrometer.observation.annotation.Observed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import no.nav.aap.api.oppslag.person.PDLConfig.Companion.PDL
import no.nav.aap.api.oppslag.person.Søker.Barn

@Component
@Observed(contextualName = PDL)
class PDLClient(private val adapter : PDLWebClientAdapter) {

    private val log = LoggerFactory.getLogger(PDLClient::class.java)

    @Retry(name =  "graphql")
    fun søkerUtenBarn() = adapter.søker()

    @Retry(name =  "graphql")
    fun harBeskyttetBarn(barn : List<Barn>) = adapter.harBeskyttetBarn(barn)

    @Retry(name =  "graphql")
    fun søkerMedBarn() = adapter.søker(true)

    override fun toString() = "${javaClass.simpleName} [pdl=$adapter]"
}