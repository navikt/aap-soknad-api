package no.nav.aap.api.søknad

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

@ConditionalOnMissingBean(SøknadKafkaProducer::class)
@Component
class LoggingSøknadSender : SøknadSender {

    private val log = LoggerFactory.getLogger(LoggingSøknadSender::class.java)
    override fun sendUtlandsSøknad(fnr: Fødselsnummer, søknad: UtenlandsSøknadView) {
        log.info("Sender søknad")
    }

}
