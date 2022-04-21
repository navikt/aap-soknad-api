package no.nav.aap.api.søknad.formidling

import io.micrometer.core.instrument.Counter
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFutureCallback


class StandardFormidlingCallback(val søknad: StandardSøknad, val counter: Counter) : ListenableFutureCallback<SendResult<String, StandardSøknad>> {
    private val secureLog = LoggerUtil.getSecureLogger()
    private val log = LoggerUtil.getLogger(javaClass)
    override fun onSuccess(result: SendResult<String, StandardSøknad>?) {
        counter.increment()
        log.info(CONFIDENTIAL,
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