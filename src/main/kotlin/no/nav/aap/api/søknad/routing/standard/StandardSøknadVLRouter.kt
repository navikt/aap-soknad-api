package no.nav.aap.api.søknad.routing.standard

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics.counter
import no.nav.aap.api.config.Counters.COUNTER_SØKNAD_MOTTATT
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.oppslag.saf.SafClient
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.joark.JoarkResponse
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.callId
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback

@Service
class StandardSøknadVLRouter(private val router: KafkaOperations<String, StandardSøknad>,
                             private val cfg: StandardSøknadVLRouterConfig, private val saf: SafClient) {

    val log = LoggerUtil.getLogger(javaClass)
    fun route(søknad: StandardSøknad, søker: Søker, dokumenter: JoarkResponse) {
        try {
            log.info("SAF $dokumenter")
            saf.dokument(dokumenter.journalpostId, dokumenter.dokumenter.first())
            log.info("SAF OK")
        }
        catch (e: Exception) {
            log.warn("OOPS", e)
        }
        return router.send(ProducerRecord(cfg.topic, søker.fødselsnummer.fnr, søknad)
            .apply {
                headers().add(NAV_CALL_ID, callId().toByteArray())
            })
            .addCallback(StandardRoutingCallback(søknad, counter(COUNTER_SØKNAD_MOTTATT)))
    }

    override fun toString() = "$javaClass.simpleName [router=$router]"
}

private class StandardRoutingCallback(private val søknad: StandardSøknad, private val counter: Counter) :
    ListenableFutureCallback<SendResult<String, StandardSøknad>> {
    private val secureLog = LoggerUtil.getSecureLogger()
    private val log = LoggerUtil.getLogger(javaClass)
    override fun onSuccess(result: SendResult<String, StandardSøknad>?) {
        counter.increment()
        with(result?.recordMetadata) {
            log.info(CONFIDENTIAL,
                    "Søknad $søknad sent til Kafka på topic ${this?.topic()}, partition ${this?.partition()} med offset  ${this?.offset()} OK",
                    secureLog.debug("Søknad $søknad sent til kafka ($result)"))
        }
    }

    override fun onFailure(e: Throwable) {
        log.error("Klarte ikke sende søknad til Kafka, se secure log for info")
        secureLog.error("Klarte ikke sende $søknad til Kafka", e)
        throw IntegrationException("Klarte ikke sende inn søknad", uri = null, e)
    }
}