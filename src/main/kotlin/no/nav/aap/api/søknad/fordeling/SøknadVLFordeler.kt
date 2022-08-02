package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.fordeling.VLFordelingConfig.VLTopicConfig
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.callId
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.core.KafkaProducerException
import org.springframework.kafka.core.KafkaSendCallback
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component

@Component
class SøknadVLFordeler(private val fordeler: KafkaOperations<String, Any>) {

    private val log = getLogger(javaClass)

    fun fordel(søknad: Any, fnr: Fødselsnummer, journalpostId: String, cfg: VLTopicConfig) =
        with(cfg) {
            if (enabled) {
                fordeler.send(ProducerRecord(topic, fnr.fnr, søknad)
                    .apply {
                        headers()
                            .add(NAV_CALL_ID, callId().toByteArray())
                            .add("journalpostid", journalpostId.toByteArray())
                    }).addCallback(FordelingCallback("søknad til VL"))
            }
            else {
                log.warn("Fordeler ikke søknad til VL")
            }
        }

    override fun toString() = "$javaClass.simpleName [fordeler=$fordeler]"
}

internal class FordelingCallback(private val msg: String) : KafkaSendCallback<String, Any> {
    private val log = getLogger(javaClass)

    override fun onSuccess(result: SendResult<String, Any>?) =
        with(result) {
            log.info("Fordelte $msg med key ${this?.producerRecord?.key()} og offset ${this?.recordMetadata?.offset()} på ${this?.recordMetadata?.topic()}")
        }

    override fun onFailure(e: KafkaProducerException) =
        throw IntegrationException(msg = "Kunne ikke fordele $msg ", cause = e)
}