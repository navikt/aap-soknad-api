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

    private val log = getLogger(javaClass)

    fun fordel(søknad: Any, fnr: Fødselsnummer, journalpostId: String, cfg: VLTopicConfig) =
        with(cfg) {
            fordeler.send(ProducerRecord(topic, fnr.fnr, søknad)
                .apply {
                    headers()
                        .add(NAV_CALL_ID, callId().toByteArray())
                        .add("journalpostid", journalpostId.toByteArray())
                })
                .whenComplete {
                    res, e -> e?.let {
                    failure("Kunne ikke sende $journalpostId til Kelvin",it as KafkaProducerException)
                } ?: success(journalpostId,fnr,res.recordMetadata)
                }
        }
    private fun success(msg: String, fnr: Fødselsnummer,rec: RecordMetadata) = log(msg ,fnr,rec)
    private fun failure(msg: String,  e: KafkaProducerException) :Nothing = throw IntegrationException(msg = msg, cause = e)

    private fun log(msg: String, fnr: Fødselsnummer, recordMetadata: RecordMetadata?) =
        with(recordMetadata) {
            log.info("Fordelte $msg for ${fnr.fnr.partialMask()}, offset ${this?.offset()}, partition ${this?.partition()} på topic ${this?.topic()}")
        }

    override fun toString() = "${javaClass.simpleName} [fordeler=$fordeler]"
}