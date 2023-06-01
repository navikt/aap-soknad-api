package no.nav.aap.api.oppslag.behandler

import io.micrometer.observation.annotation.Observed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import no.nav.aap.api.oppslag.person.PDLClient

@Component
@Observed
class BehandlerClient(private val adapter : BehandlerWebClientAdapter) {

    private val log = LoggerFactory.getLogger(PDLClient::class.java)


    fun behandlerInfo() : List<RegistrertBehandler> {
        log.trace("Behandler ASYNC start")
        return adapter.behandlerInfo().also {
            log.trace("Behandler ASYNC end")
        }
    }
}