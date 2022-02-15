package no.nav.aap.api.søknad

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.oppslag.pdl.PDLOperations
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY
import org.springframework.kafka.support.KafkaHeaders.TOPIC
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service


@Service
class KafkaSøknadFormidler(
        private val authContext: AuthContext,
        private val pdl: PDLClient,
        private val formidler: KafkaOperations<Fødselsnummer, SøknadKafka>,
        @Value("#{'\${utenlands.topic:aap.aap-soknad-sendt.v1}'}") val søknadTopic: String) {

    private val log = LoggerUtil.getLogger(javaClass)

    fun formidle() {
        log.info(CONFIDENTIAL, "Formidler søknad for {}", authContext.getFnr())
        formidle(SøknadKafka(authContext.getFnr(), pdl.søker()?.fødseldato))
    }

    private fun formidle(søknad: SøknadKafka) =
        formidler.send(
                MessageBuilder
                    .withPayload(søknad)
                    .setHeader(MESSAGE_KEY, søknad.id)
                    .setHeader(TOPIC, søknadTopic)
                    .setHeader(NAV_CALL_ID, MDCUtil.callId())
                    .build())
            .addCallback(FormidlingCallback(søknad))

    override fun toString() = "${javaClass.simpleName} [formidler=$formidler,pdl=$pdl]"
}