package no.nav.aap.api.søknad

import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Metrics.counter
import io.micrometer.core.instrument.Tags
import no.nav.aap.api.config.Counters.COUNTER_SØKNAD_UTLAND_MOTTATT
import no.nav.aap.api.config.Counters.TAG_LAND
import no.nav.aap.api.config.Counters.TAG_VARIGHET
import no.nav.aap.api.felles.UtenlandsSøknadKafka
import no.nav.aap.api.felles.error.IntegrationException
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
class UtenlandsSøknadKafkaFormidler(
        private val formidler: KafkaOperations<String, UtenlandsSøknadKafka>,
        @Value("#{'\${utenlands.topic:aap.aap-utland-soknad-sendt.v1}'}") val søknadTopic: String) {

    fun formidle(søknad: UtenlandsSøknadKafka) =
        formidler.send(
                MessageBuilder
                    .withPayload(søknad)
                    .setHeader(MESSAGE_KEY, søknad.søker.fnr.fnr)
                    .setHeader(TOPIC, søknadTopic)
                    .setHeader(NAV_CALL_ID, MDCUtil.callId())
                    .build())
            .addCallback(UtenlandsFormidlingCallback(søknad))

    override fun toString() = "$javaClass.simpleName [kafkaOperations=$formidler]"
}


class UtenlandsFormidlingCallback(val søknad: UtenlandsSøknadKafka) :
    ListenableFutureCallback<SendResult<String, UtenlandsSøknadKafka>> {
    private val log = LoggerUtil.getLogger(javaClass)
    private val secureLog = LoggerUtil.getSecureLogger()

    override fun onSuccess(result: SendResult<String, UtenlandsSøknadKafka>?) {
        counter(COUNTER_SØKNAD_UTLAND_MOTTATT,
                Tags.of(TAG_LAND, søknad.land.alpha3,
                        TAG_VARIGHET, søknad.periode.varighetDager.toString()))
            .increment()
        log.info(
                "Søknad sent til Kafka på topic {}, partition {} med offset {} OK",
                result?.recordMetadata?.topic(),
                result?.recordMetadata?.partition(),
                result?.recordMetadata?.offset())
    }

    override fun onFailure(e: Throwable) {
        log.error("Klarte ikke sende søknad til Kafka, se secure log for info")
        secureLog.error("Klarte ikke sende $søknad til Kafka", e)
        throw IntegrationException("Klarte ikke sende inn søknad", uri = null, e)
    }
}