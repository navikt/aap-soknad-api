package no.nav.aap.api.søknad.fordeling.utland

import io.micrometer.core.instrument.Metrics.counter
import io.micrometer.core.instrument.Tags
import no.nav.aap.api.config.Counters.COUNTER_SØKNAD_UTLAND_MOTTATT
import no.nav.aap.api.config.Counters.TAG_LAND
import no.nav.aap.api.config.Counters.TAG_VARIGHET
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.fordeling.VLFordelingConfig
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.callId
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import org.springframework.util.concurrent.ListenableFutureCallback

@Component
class UtlandSøknadVLLeverandør(private val router: KafkaOperations<String, UtlandSøknad>,
                               private val cfg: VLFordelingConfig) {

    private val log = LoggerUtil.getLogger(javaClass)

    fun route(søknad: UtlandSøknad, søker: Søker, journalpostId: String) =
        with(cfg.utland) {
            if (enabled) {
                router.send(ProducerRecord(topic, søker.fnr.fnr, søknad)
                    .apply {
                        headers()
                            .add(NAV_CALL_ID, callId().toByteArray())
                            .add("journalpostid", journalpostId.toByteArray())

                    })
                    .addCallback(UtlandLeverandørCallback(søknad))
            }
            else {
                log.warn("Ruter ikke søknad til VL")
            }
        }

    override fun toString() = "$javaClass.simpleName [router=$router]"
}

private class UtlandLeverandørCallback(private val søknad: UtlandSøknad) :
    ListenableFutureCallback<SendResult<String, UtlandSøknad>> {
    private val log = LoggerUtil.getLogger(javaClass)
    private val secureLog = LoggerUtil.getSecureLogger()
    override fun onSuccess(result: SendResult<String, UtlandSøknad>?) {
        counter(COUNTER_SØKNAD_UTLAND_MOTTATT,
                Tags.of(TAG_LAND, søknad.land.alpha3,
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