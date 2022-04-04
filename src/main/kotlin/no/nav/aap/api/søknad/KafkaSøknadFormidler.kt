package no.nav.aap.api.søknad

import io.micrometer.core.instrument.Metrics
import no.nav.aap.api.config.Counters
import no.nav.aap.api.config.Counters.COUNTER_SØKNAD_MOTTATT
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
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
        private val authContext: AuthContext,
        private val pdl: PDLClient,
        private val formidler: KafkaOperations<String, SøknadKafka>,
        @Value("#{'\${utenlands.topic:aap.aap-soknad-sendt.v1}'}") val søknadTopic: String) {

    private val log = LoggerUtil.getLogger(javaClass)

    fun formidle() {
        log.info(CONFIDENTIAL, "Formidler søknad for {}", authContext.getFnr())
        formidle(SøknadKafka(authContext.getFnr(), pdl.søkerUtenBarn()?.fødseldato))
    }

    private fun formidle(søknad: SøknadKafka) =
        formidler.send(
                MessageBuilder
                    .withPayload(søknad)
                    .setHeader(MESSAGE_KEY, søknad.id)
                    .setHeader(TOPIC, søknadTopic)
                    .setHeader(NAV_CALL_ID, MDCUtil.callId())
                    .build())
            .addCallback(FormidlingCallback(søknad))

    override fun toString() = "${javaClass.simpleName} [formidler=$formidler,pdl=$pdl]"
}

class FormidlingCallback(val søknad: SøknadKafka) :
    ListenableFutureCallback<SendResult<String, SøknadKafka>> {
    private val log = LoggerUtil.getLogger(javaClass)
    private val secureLog = LoggerUtil.getSecureLogger()

    override fun onSuccess(result: SendResult<String, SøknadKafka>?) {
        Metrics.counter(COUNTER_SØKNAD_MOTTATT).increment()
        log.info(
                "Søknad $søknad sent til Kafka på topic {}, partition {} med offset {} OK",
                result?.recordMetadata?.topic(),
                result?.recordMetadata?.partition(),
                result?.recordMetadata?.offset())
        secureLog.debug("Søknad $søknad sent til kafka ($result)")
    }

    override fun onFailure(e: Throwable) {
        log.error("Klarte ikke sende søknad til Kafka, se secure log for info")
        secureLog.error("Klarte ikke sende $søknad til Kafka", e)
        throw IntegrationException("Klarte ikke sende inn søknad", uri = null, e)
    }
}