package no.nav.aap.api.søknad

import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Tags
import no.nav.aap.api.config.Counters
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.UtenlandsSøknadKafka
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.util.LoggerUtil
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFutureCallback

class UtenlandsFormidlingCallback(val søknad: UtenlandsSøknadKafka) :
    ListenableFutureCallback<SendResult<String, UtenlandsSøknadKafka>> {
    private val log = LoggerUtil.getLogger(javaClass)
    private val secureLog = LoggerUtil.getSecureLogger()

    override fun onSuccess(result: SendResult<String, UtenlandsSøknadKafka>?) {
        Metrics.counter(
                Counters.COUNTER_SØKNAD_UTLAND_MOTTATT,
                Tags.of(
                        Counters.TAG_LAND, søknad.land.alpha3,
                        Counters.TAG_VARIGHET, søknad.periode.varighetDager().toString()))
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