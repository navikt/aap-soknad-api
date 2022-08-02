package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.SendCallback
import no.nav.aap.api.søknad.fordeling.VLFordelingConfig.VLTopicConfig
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.callId
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaOperations
import org.springframework.stereotype.Component

@Component
class SøknadVLFordeler(private val fordeler: KafkaOperations<String, Any>) {

    private val log = getLogger(javaClass)

    fun fordel(søknad: Any, fnr: Fødselsnummer, journalpostId: String, topicConfig: VLTopicConfig) =
        with(topicConfig) {
            if (enabled) {
                fordeler.send(ProducerRecord(topic, fnr.fnr, søknad)
                    .apply {
                        headers()
                            .add(NAV_CALL_ID, callId().toByteArray())
                            .add("journalpostid", journalpostId.toByteArray())
                    }).addCallback(SendCallback("søknad til VL med journalpost $journalpostId"))
            }
            else {
                log.warn("Fordeler ikke søknad til VL")
            }
        }

    override fun toString() = "$javaClass.simpleName [fordeler=$fordeler]"
}