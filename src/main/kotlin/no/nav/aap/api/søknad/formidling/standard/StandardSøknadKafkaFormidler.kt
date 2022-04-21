package no.nav.aap.api.søknad.formidling.standard

import io.micrometer.core.instrument.Metrics
import no.nav.aap.api.config.Counters
import no.nav.aap.api.søknad.formidling.StandardFormidlingCallback
import no.nav.aap.api.søknad.formidling.SøknadFormidler
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.util.MDCUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class StandardSøknadKafkaFormidler(private val formidler: KafkaOperations<String, StandardSøknad>, @Value("#{'\${standard.ny.topic:aap.aap-soknad-sendt-ny.v1}'}") val søknadTopic: String) :
    SøknadFormidler<Unit> {
    override fun formidle(søknad: StandardSøknad, søker: Søker) =
        formidler.send(
                MessageBuilder.withPayload(søknad)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, søker.fødselsnummer.fnr)
                    .setHeader(KafkaHeaders.TOPIC, søknadTopic)
                    .setHeader(MDCUtil.NAV_CALL_ID, MDCUtil.callId())
                    .build())
            .addCallback(StandardFormidlingCallback(søknad, Metrics.counter(Counters.COUNTER_SØKNAD_MOTTATT)))
}