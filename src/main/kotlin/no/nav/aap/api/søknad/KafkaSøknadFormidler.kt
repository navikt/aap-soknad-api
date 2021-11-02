package no.nav.aap.api.søknad

import no.nav.aap.api.error.IntegrationException
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.model.UtenlandsSøknadKafka
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
import no.nav.aap.api.søknad.model.toKafkaObject
import no.nav.aap.api.util.LoggerUtil
import no.nav.aap.api.util.MDCUtil
import no.nav.aap.api.util.MDCUtil.NAV_CALL_ID
import no.nav.boot.conditionals.ConditionalOnGCP
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.KafkaHeaders.TOPIC
import org.springframework.kafka.support.SendResult
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback




@Service
class KafkaSøknadFormidler(private val kafkaOperations: KafkaOperations<String, UtenlandsSøknadKafka>, @Value("#{'\${utenlands.topic}'}") private val søknadTopic: String) : SøknadFormidler{
    private val log = LoggerFactory.getLogger(KafkaSøknadFormidler::class.java)
    private val secureLog = LoggerUtil.getSecureLogger()


    override fun sendUtlandsSøknad(fnr: Fødselsnummer, søknad: UtenlandsSøknadView) {
            send(
                MessageBuilder
                .withPayload(søknad.toKafkaObject(fnr.fnr))
                .setHeader(TOPIC, søknadTopic)
                .setHeader(NAV_CALL_ID, MDCUtil.callId())
                .build())
    }

    private fun send(melding: Message<UtenlandsSøknadKafka>) {
        log.info("Søknad sendes til Kafka på topic {}", søknadTopic)
        kafkaOperations.send(melding)
            .addCallback(object : ListenableFutureCallback<SendResult<String, UtenlandsSøknadKafka>> {
                override fun onSuccess(result: SendResult<String, UtenlandsSøknadKafka>?) {
                    log.info("Søknad sent til Kafka på topic {} med offset {} OK", søknadTopic,result?.recordMetadata?.offset())
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