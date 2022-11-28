package no.nav.aap.api.søknad

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.partialMask
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.kafka.support.ProducerListener

class SendCallback<K, V>(private val msg: String) : ProducerListener<K, V> {
    private val log = getLogger(javaClass)

    override fun onSuccess(producerRecord: ProducerRecord<K, V>?, recordMetadata: RecordMetadata?) {
        when (val key = producerRecord?.key()) {
            is NokkelInput -> log(key.fodselsnummer.partialMask(), recordMetadata)
            is String -> log(key.partialMask(), recordMetadata)
            else -> log("$key", recordMetadata)
        }
    }

    override fun onError(producerRecord: ProducerRecord<K, V>, recordMetadata: RecordMetadata?, e: Exception) {
        when (val key =producerRecord.key()) {
            is NokkelInput -> feil(key.fodselsnummer.partialMask(), e)
            is String -> feil(key.partialMask(), e)
            else -> feil("$key", e)
        }
    }

    private fun log(fnr: String, recordMetadata: RecordMetadata?) {
        with(recordMetadata) {
            log.info("Sendte $msg for ${fnr.partialMask()}, offset ${this?.offset()}, partition ${this?.partition()} på topic ${this?.topic()}")
        }
    }

    private fun feil(key: String, e: Exception): Nothing =
        throw IntegrationException(msg = "Kunne ikke sende $msg for key $key", cause = e)


}