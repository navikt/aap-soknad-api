package no.nav.aap.api.søknad.routing

import io.micrometer.core.instrument.Metrics.counter
import io.micrometer.core.instrument.Tags
import no.nav.aap.api.config.Counters.COUNTER_SØKNAD_UTLAND_MOTTATT
import no.nav.aap.api.config.Counters.TAG_LAND
import no.nav.aap.api.config.Counters.TAG_VARIGHET
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.joark.pdf.PDFGeneratorWebClientAdapter.UtlandData
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.callId
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback


@Service
class UtlandSøknadVLRouter(private val router: KafkaOperations<String, UtlandData>,
                           @Value("#{'\${utenlands.topic:aap.aap-utland-soknad-sendt.v1}'}") private val søknadTopic: String) {

    fun route(søknad: UtlandData) =
        router.send(ProducerRecord(søknadTopic, søknad.fødselsnummer.fnr, søknad)
            .apply {
                headers().add(NAV_CALL_ID, callId().toByteArray())
            })
            .addCallback(UtlandRouterCallback(søknad))
    override fun toString() = "$javaClass.simpleName [router=$router]"
}

private class UtlandRouterCallback(private val søknad: UtlandData) : ListenableFutureCallback<SendResult<String, UtlandData>> {
    private val log = LoggerUtil.getLogger(javaClass)
    private val secureLog = LoggerUtil.getSecureLogger()
    override fun onSuccess(result: SendResult<String, UtlandData>?) {
        counter(COUNTER_SØKNAD_UTLAND_MOTTATT,
                Tags.of(TAG_LAND, søknad.landKode.alpha3,
                        TAG_VARIGHET, søknad.periode.varighetDager.toString()))
            .increment()
        log.info("Søknad sent til Kafka på topic {}, partition {} med offset {} OK",
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