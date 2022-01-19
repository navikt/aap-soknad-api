package no.nav.aap.api.søknad

import io.micrometer.core.instrument.Metrics
import no.nav.aap.api.config.Counters
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.util.LoggerUtil
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFutureCallback

class FormidlingCallback(val søknad: SøknadKafka) :
    ListenableFutureCallback<SendResult<Fødselsnummer, SøknadKafka>> {
    private val log = LoggerUtil.getLogger(javaClass)
    private val secureLog = LoggerUtil.getSecureLogger()

    override fun onSuccess(result: SendResult<Fødselsnummer, SøknadKafka>?) {
        Metrics.counter(Counters.COUNTER_SØKNAD_MOTTATT).increment()
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