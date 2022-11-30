package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.fordeling.VLFordelingConfig.VLTopicConfig
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.callId
import no.nav.aap.util.StringExtensions.partialMask
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.core.KafkaProducerException
import org.springframework.stereotype.Component

@Component
class SøknadVLFordeler(private val fordeler: KafkaOperations<String, Any>) {

    fun fordel(søknad: Any, fnr: Fødselsnummer, journalpostId: String, cfg: VLTopicConfig) =
        with(cfg) {
            fordeler.send(ProducerRecord(topic, fnr.fnr, søknad)
                .apply {
                    headers()
                        .add(NAV_CALL_ID, callId().toByteArray())
                        .add("journalpostid", journalpostId.toByteArray())
                })
                .whenComplete { res, e ->
                    e?.let {
                        throw IntegrationException(msg = "Kunne ikke sende $journalpostId til Kelvin", cause = it as KafkaProducerException)
                    } ?: log(journalpostId, fnr, res.recordMetadata)
                }
        }

    override fun toString() = "${javaClass.simpleName} [fordeler=$fordeler]"

    companion object {
        private fun log(msg: String, fnr: Fødselsnummer, recordMetadata: RecordMetadata) =
            with(recordMetadata) {
                log.info("Fordelte $msg for ${fnr.fnr.partialMask()}, offset ${offset()}, partition ${partition()} på topic ${topic()}")
            }

        private val log = getLogger(SøknadVLFordeler::class.java)
    }
}