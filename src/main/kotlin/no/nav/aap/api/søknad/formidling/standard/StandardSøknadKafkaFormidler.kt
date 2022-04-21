package no.nav.aap.api.søknad.formidling.standard

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics.*
import no.nav.aap.api.config.Counters.COUNTER_SØKNAD_MOTTATT
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.formidling.SøknadFormidler
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.callId
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
class StandardSøknadKafkaFormidler(private val formidler: KafkaOperations<String, StandardSøknad>,
                                   @Value("#{'\${standard.ny.topic:aap.aap-soknad-sendt-ny.v1}'}") val søknadTopic: String) :
    SøknadFormidler<Unit> {
    override fun formidle(søknad: StandardSøknad, søker: Søker) =
        formidler.send(
                MessageBuilder.withPayload(søknad)
                    .setHeader(MESSAGE_KEY, søker.fødselsnummer.fnr)
                    .setHeader(TOPIC, søknadTopic)
                    .setHeader(NAV_CALL_ID, callId())
                    .build())
            .addCallback(StandardFormidlingCallback(søknad, counter(COUNTER_SØKNAD_MOTTATT)))
}
private class StandardFormidlingCallback(val søknad: StandardSøknad, val counter: Counter) : ListenableFutureCallback<SendResult<String, StandardSøknad>> {
    private val secureLog = LoggerUtil.getSecureLogger()
    private val log = LoggerUtil.getLogger(javaClass)
    override fun onSuccess(result: SendResult<String, StandardSøknad>?) {
        counter.increment()
        log.info(
                CONFIDENTIAL,
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