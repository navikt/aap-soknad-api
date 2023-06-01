package no.nav.aap.api.oppslag.kontaktinformasjon

import io.micrometer.observation.annotation.Observed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import no.nav.aap.api.oppslag.person.PDLClient

@Component
@Observed
class KRRClient(private val adapter : KRRWebClientAdapter) {

    private val log = LoggerFactory.getLogger(PDLClient::class.java)


    fun kontaktInfo() : Kontaktinformasjon? {
        log.trace("KRR ASYNC konto start")
        return adapter.kontaktInformasjon().also {
            log.trace("KRR ASYNC konto end")
        }
    }
}