package no.nav.aap.api.søknad

import io.micrometer.core.instrument.Metrics.counter
import io.micrometer.core.instrument.Tags
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Søker
import no.nav.aap.api.felles.UtenlandsSøknadKafka
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.oppslag.pdl.PDLOperations
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
import no.nav.aap.api.søknad.model.toKafkaObject
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
class KafkaUtenlandsSøknadFormidler(
        private val pdl: PDLOperations,
        private val formidler: KafkaOperations<Fødselsnummer, UtenlandsSøknadKafka>,
        @Value("#{'\${utenlands.topic:aap.aap-utland-soknad-sendt.v1}'}") val søknadTopic: String) {

    private val log = LoggerUtil.getLogger(javaClass)
    private val secureLog = LoggerUtil.getSecureLogger()

    fun formidle(fnr: Fødselsnummer, søknad: UtenlandsSøknadView) {
        send(søknad.toKafkaObject(Søker(fnr, pdl.person()?.navn)))
    }

    private fun send(søknad: UtenlandsSøknadKafka) =
        formidler.send(
                MessageBuilder
                    .withPayload(søknad)
                    .setHeader(MESSAGE_KEY, søknad.søker.fnr)
                    .setHeader(TOPIC, søknadTopic)
                    .setHeader(NAV_CALL_ID, MDCUtil.callId())
                    .build())
            .addCallback(object : ListenableFutureCallback<SendResult<Fødselsnummer, UtenlandsSøknadKafka>> {
                override fun onSuccess(result: SendResult<Fødselsnummer, UtenlandsSøknadKafka>?) {
                    counter(
                            COUNTER_SØKNAD_UTLAND_MOTTATT,
                            Tags.of(
                                    TAG_LAND, søknad.land.alpha3,
                                    TAG_VARIGHET, søknad.periode.varighetDager().toString()))
                        .increment()
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

    override fun toString() = "${javaClass.simpleName} [kafkaOperations=$formidler,pdl=$pdl]"

    companion object {
        private const val TAG_LAND = "land"
        private const val TAG_VARIGHET = "varighet"
        private const val COUNTER_SØKNAD_UTLAND_MOTTATT = "aap_soknad_utland_mottatt"

    }

}