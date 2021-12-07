package no.nav.aap.api.søknad

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
class KafkaSøknadFormidler(
        private val søknadMetrics: SøknadMetrics,
        private val pdl: PDLOperations,
        private val kafkaOperations: KafkaOperations<Fødselsnummer, UtenlandsSøknadKafka>,
        @Value("#{'\${utenlands.topic:aap.aap-utland-soknad-sendt.v1}'}") val søknadTopic: String
                          ) : SøknadFormidler {

    private val log = LoggerUtil.getLogger(javaClass)
    private val secureLog = LoggerUtil.getSecureLogger()

    override fun sendUtenlandsSøknad(fnr: Fødselsnummer, søknad: UtenlandsSøknadView) =
        send(søknad.toKafkaObject(Søker(fnr, pdl.navn())))

    private fun send(søknad: UtenlandsSøknadKafka) =
        kafkaOperations.send(
                MessageBuilder
                    .withPayload(søknad)
                    .setHeader(MESSAGE_KEY, søknad.søker.fnr)
                    .setHeader(TOPIC, søknadTopic)
                    .setHeader(NAV_CALL_ID, MDCUtil.callId())
                    .build())
            .addCallback(object : ListenableFutureCallback<SendResult<Fødselsnummer, UtenlandsSøknadKafka>> {
                override fun onSuccess(result: SendResult<Fødselsnummer, UtenlandsSøknadKafka>?) {
                    log.info(
                            "Søknad sent til Kafka på topic {}, partition {} med offset {} OK",
                            søknadTopic,
                            result?.recordMetadata?.partition(),
                            result?.recordMetadata?.offset())
                    secureLog.debug("Søknad $søknad sent til kafka ($result)")
                    søknadMetrics.increment(søknad)
                }
                override fun onFailure(e: Throwable) {
                    log.error("Klarte ikke sende søknad til Kafka, se secure log for info")
                    secureLog.error("Klarte ikke sende $søknad til Kafka", e)
                    throw IntegrationException("Klarte ikke sende inn søknad", uri=null, e)
                }
            })

    override fun toString() = "${javaClass.simpleName} [kafkaOperations=$kafkaOperations,pdl=$pdl]"

}