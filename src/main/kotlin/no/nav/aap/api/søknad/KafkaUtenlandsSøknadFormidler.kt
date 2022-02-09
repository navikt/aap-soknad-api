package no.nav.aap.api.søknad

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Søker
import no.nav.aap.api.felles.UtenlandsSøknadKafka
import no.nav.aap.api.oppslag.pdl.PDLOperations
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.boot.conditionals.EnvUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY
import org.springframework.kafka.support.KafkaHeaders.TOPIC
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service


@Service
class KafkaUtenlandsSøknadFormidler(
        private val authContext: AuthContext,
        private val pdl: PDLOperations,
        private val formidler: KafkaOperations<Fødselsnummer, UtenlandsSøknadKafka>,
        @Value("#{'\${utenlands.topic:aap.aap-utland-soknad-sendt.v1}'}") val søknadTopic: String) {

    private val log = LoggerUtil.getLogger(javaClass)

    fun formidle(søknad: UtenlandsSøknadView) {
        log.info(EnvUtil.CONFIDENTIAL, "Formidler utenlandssøknad for ${authContext.getFnr()}")
        log.info(EnvUtil.CONFIDENTIAL, "Med barn for ${pdl.søker(true)}")
        formidle(søknad.toKafkaObject(Søker(authContext.getFnr(), pdl.søker()?.navn)))
    }

    private fun formidle(søknad: UtenlandsSøknadKafka) =
        formidler.send(
                MessageBuilder
                    .withPayload(søknad)
                    .setHeader(MESSAGE_KEY, søknad.søker.fnr)
                    .setHeader(TOPIC, søknadTopic)
                    .setHeader(NAV_CALL_ID, MDCUtil.callId())
                    .build())
            .addCallback(UtenlandsFormidlingCallback(søknad))

    override fun toString() = "${javaClass.simpleName} [kafkaOperations=$formidler,pdl=$pdl]"

}