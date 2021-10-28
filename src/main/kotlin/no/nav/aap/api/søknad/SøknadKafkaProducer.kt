package no.nav.aap.api.søknad

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
import no.nav.aap.api.søknad.model.toKafkaObject
import no.nav.aap.api.util.LoggerUtil
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class SøknadKafkaProducer(
    private val aivenKafkaProducerTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
    private val secureLog = LoggerUtil.getSecureLogger()
    private val søknadTopic = "aap-soknad-sendt.v1"

    fun sendUtlandsSøknad(fnr: Fødselsnummer, søknad: UtenlandsSøknadView) {
        runCatching {
            val søknadToSend = søknad.toKafkaObject(fnr.fnr)
            val result = aivenKafkaProducerTemplate.send(søknadTopic, søknadToSend.toJson()).get()
            log.info("Søknad sent til Kafka.")
            secureLog.debug("Søknad $søknadToSend sent til kafka ($result)")
        }.onFailure {
            log.error("Klarte ikke sende søknad til Kafka, se secure log for info")
            secureLog.error("Klarte ikke sende til Kafka", it)
            throw RuntimeException("Klarte ikke sende inn søknad",it)
        }
    }

    private fun Any.toJson() = objectMapper.writeValueAsString(this)
}
