package no.nav.aap.api.søknad

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.partialMask
import org.springframework.kafka.core.KafkaProducerException
import org.springframework.kafka.core.KafkaSendCallback
import org.springframework.kafka.support.SendResult

class SendCallback<K, V>(private val msg: String) : KafkaSendCallback<K, V> {
    private val log = getLogger(javaClass)

    override fun onSuccess(result: SendResult<K, V>?) =
        with(result) {
            log.info("Sendte $msg med nøkkel ${(this?.producerRecord?.key() as String).partialMask()}  og offset ${this?.recordMetadata?.offset()} på ${this?.recordMetadata?.topic()}")
        }

    override fun onFailure(e: KafkaProducerException) =
        throw IntegrationException(msg = "Kunne ikke sende $msg med nøkkel ${
            (e.getFailedProducerRecord<K, V>().key() as String).partialMask()
        }", cause = e)
}