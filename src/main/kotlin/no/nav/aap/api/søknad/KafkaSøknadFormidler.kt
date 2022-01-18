package no.nav.aap.api.søknad

import io.micrometer.core.instrument.Metrics.counter
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.oppslag.pdl.PDLOperations
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY
import org.springframework.kafka.support.KafkaHeaders.TOPIC
import org.springframework.kafka.support.SendResult
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback


@Service
class KafkaSøknadFormidler(
        private val pdl: PDLOperations,
        private val kafkaOperations: KafkaOperations<Fødselsnummer, SøknadKafka>,
        @Value("#{'\${utenlands.topic:aap.aap-soknad-sendt.v1}'}") val søknadTopic: String) {

    private val log = LoggerUtil.getLogger(javaClass)
    private val secureLog = LoggerUtil.getSecureLogger()

    fun sendSøknad(fnr: Fødselsnummer) {
        val p = pdl.person()
        log.info("Fødselsdato {}", p?.fødseldato)
        send(SøknadKafka(fnr, p?.fødseldato))
    }

    private fun send(søknad: SøknadKafka) =
        kafkaOperations.send(
                MessageBuilder
                    .withPayload(søknad)
                    .setHeader(MESSAGE_KEY, søknad.ident.verdi)
                    .setHeader(TOPIC, søknadTopic)
                    .setHeader(NAV_CALL_ID, MDCUtil.callId())
                    .build())
            .addCallback(object : ListenableFutureCallback<SendResult<Fødselsnummer, SøknadKafka>> {
                override fun onSuccess(result: SendResult<Fødselsnummer, SøknadKafka>?) {
                    counter(COUNTER_SØKNAD_MOTTATT).increment()
                    log.info(
                            "Søknad sent til Kafka på topic {}, partition {} med offset {} OK",
                            søknadTopic,
                            result?.recordMetadata?.partition(),
                            result?.recordMetadata?.offset())
                    secureLog.debug("Søknad $søknad sent til kafka ($result)")
                }

                override fun onFailure(e: Throwable) {
                    log.error("Klarte ikke sende søknad til Kafka, se secure log for info")
                    secureLog.error("Klarte ikke sende $søknad til Kafka", e)
                    throw IntegrationException("Klarte ikke sende inn søknad", uri = null, e)
                }
            })

    override fun toString() = "${javaClass.simpleName} [kafkaOperations=$kafkaOperations,pdl=$pdl]"

    companion object {
        private const val COUNTER_SØKNAD_MOTTATT = "aap_soknad_mottatt"
    }
}