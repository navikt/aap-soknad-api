package no.nav.aap.api.søknad.routing.legacy

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics.counter
import no.nav.aap.api.config.Counters.COUNTER_SØKNAD_MOTTATT
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.util.AuthContext
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
@Deprecated("Kun for enkel testing")
class LegacyStandardSøknadVLRouter(private val ctx: AuthContext,
                                   private val pdl: PDLClient,
                                   private val router: KafkaOperations<String, LegacyStandardSøknadKafka>,
                                   @Value("#{'\${standard.topic:aap.aap-soknad-sendt.v1}'}")  private val søknadTopic: String) {

    fun route() = route(LegacyStandardSøknadKafka(ctx.getFnr(), pdl.søkerUtenBarn().fødseldato))
    fun route(søknad: LegacyStandardSøknadKafka) {
        router.send(ProducerRecord(søknadTopic, søknad.id, søknad)
            .apply {
                headers().add(NAV_CALL_ID, callId().toByteArray())
            })
            .addCallback(RouterCallback(søknad, counter(COUNTER_SØKNAD_MOTTATT)))
    }
    override fun toString() = "$javaClass.simpleName [formidler=$router,pdl=$pdl]"
}

private class RouterCallback(private val søknad: LegacyStandardSøknadKafka, private val counter: Counter) :
    ListenableFutureCallback<SendResult<String, LegacyStandardSøknadKafka>> {
    private val log = LoggerUtil.getLogger(javaClass)
    private val secureLog = LoggerUtil.getSecureLogger()

    override fun onSuccess(result: SendResult<String, LegacyStandardSøknadKafka>?) {
        counter.increment()
        log.info("Søknad $søknad sent til Kafka på topic {}, partition {} med offset {} OK",
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