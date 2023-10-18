package no.nav.aap.api.søknad.fordeling

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaOperations
import org.springframework.stereotype.Component
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.callId

@Component
class SøknadVLFordeler(private val fordeler: KafkaOperations<String, Any>) {

    private val log = getLogger(javaClass)

    fun fordel(søknad: Any, fnr: Fødselsnummer, journalpostId: String, cfg: VLFordelingConfig.VLTopicConfig) =
        if (cfg.enabled) {
            with(cfg) {
                fordeler.send(ProducerRecord(topic, fnr.fnr, søknad)
                    .apply {
                        headers()
                            .add(NAV_CALL_ID, callId().toByteArray())
                            .add("journalpostid", journalpostId.toByteArray())
                    }).get().also {
                    log.trace("Sendte ${søknad.javaClass.simpleName.lowercase()}  til VL med journalpost $journalpostId  på offset ${it.recordMetadata.offset()} partition${it.recordMetadata.partition()}på topic ${it.recordMetadata.topic()}")
                }
                Unit
            }
        }
        else {
            log.trace("Fordeler ikke ${søknad.javaClass.simpleName.lowercase()}  til VL")
        }

    override fun toString() = "${javaClass.simpleName} [fordeler=$fordeler]"
}