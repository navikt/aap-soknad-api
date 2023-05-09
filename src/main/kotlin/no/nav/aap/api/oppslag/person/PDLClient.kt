package no.nav.aap.api.oppslag.person

import io.micrometer.observation.annotation.Observed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import no.nav.aap.api.oppslag.person.PDLConfig.Companion.PDL
import no.nav.aap.api.oppslag.person.Søker.Barn

@Component
@Observed(contextualName = PDL)
class PDLClient(private val adapter : PDLWebClientAdapter) {

    private val log = LoggerFactory.getLogger(PDLClient::class.java)

    fun søkerUtenBarn() = runCatching { adapter.søker1() }.getOrElse {
        log.warn("PDL ny uten barn feil",it)
        adapter.søker(false)  }

    fun harBeskyttetBarn(barn : List<Barn>) = runCatching { adapter.harBeskyttetBarn1(barn) }.getOrElse {
        log.warn("PDL ny beskyttet barn feil",it)
        adapter.harBeskyttetBarn(barn)
    }

    fun søkerMedBarn() = runCatching { adapter.søker1(true) }.getOrElse {
        log.warn("PDL ny med barn feil",it)
        adapter.søker(true)
    }

    override fun toString() = "${javaClass.simpleName} [pdl=$adapter]"
}