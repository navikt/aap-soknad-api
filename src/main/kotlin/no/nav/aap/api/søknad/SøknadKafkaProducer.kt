package no.nav.aap.api.søknad

import no.nav.aap.api.error.IntegrationException
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.model.UtenlandsSøknadKafka
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
import no.nav.aap.api.søknad.model.toKafkaObject
import no.nav.aap.api.util.LoggerUtil
import no.nav.aap.api.util.MDCUtil
import no.nav.aap.api.util.MDCUtil.NAV_CALL_ID
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.KafkaHeaders.TOPIC
import org.springframework.kafka.support.SendResult
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback




@Service
class SøknadKafkaProducer(private val kafkaOperations: KafkaOperations<String, UtenlandsSøknadKafka>) {
    private val log = LoggerFactory.getLogger(SøknadKafkaProducer::class.java)
    private val secureLog = LoggerUtil.getSecureLogger()
    private val søknadTopic = "aap-utland-soknad-sendt.v1"

    fun sendUtlandsSøknad(fnr: Fødselsnummer, søknad: UtenlandsSøknadView) {
            send(
                MessageBuilder
                .withPayload(søknad.toKafkaObject(fnr.fnr))
                .setHeader(TOPIC, søknadTopic)
                .setHeader(NAV_CALL_ID, MDCUtil.callId())
                .build())
    }

    private fun send(melding: Message<UtenlandsSøknadKafka>) {
        kafkaOperations.send(melding)
            .addCallback(object : ListenableFutureCallback<SendResult<String, UtenlandsSøknadKafka>> {
                override fun onSuccess(result: SendResult<String, UtenlandsSøknadKafka>?) {
                    log.info("Søknad sent til Kafka med offset {} OK", result?.recordMetadata?.offset())
                    secureLog.debug("Søknad $melding sent til kafka ($result)")
                }
                override fun onFailure(e: Throwable) {
                    log.error("Klarte ikke sende søknad til Kafka, se secure log for info")
                    secureLog.error("Klarte ikke sende $melding til Kafka", e)
                    throw IntegrationException("Klarte ikke sende inn søknad", e)
                }
            });
    }
}
