package no.nav.aap.api.søknad

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.partialMask
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.kafka.core.KafkaProducerException
import org.springframework.kafka.core.KafkaSendCallback
import org.springframework.kafka.support.SendResult

class SendCallback<K, V>(private val msg: String) : KafkaSendCallback<K, V> {
    private val log = getLogger(javaClass)

    override fun onSuccess(result: SendResult<K, V>?) =
        with(result) {
            when (val key = this?.producerRecord?.key()) {
                is NokkelInput -> log(key.fodselsnummer.partialMask(), this?.recordMetadata)
                is String -> log(key.partialMask(), this?.recordMetadata)
                else -> log("$key", this?.recordMetadata)
            }
        }

    override fun onFailure(e: KafkaProducerException) =
        when (val key = e.getFailedProducerRecord<K, V>().key()) {
            is NokkelInput -> feil(key.fodselsnummer.partialMask(), e)
            is String -> feil(key.partialMask(), e)
            else -> feil("$key", e)
        }

    private fun log(fnr: String, recordMetadata: RecordMetadata?) {
        log.info("Sendte $msg med nøkkel $fnr  og offset $recordMetadata?.offset()} på ${recordMetadata?.topic()}")
    }

    private fun feil(key: String, e: KafkaProducerException): Nothing =
        throw IntegrationException(msg = "Kunne ikke sende $msg for key  $key", cause = e)
}