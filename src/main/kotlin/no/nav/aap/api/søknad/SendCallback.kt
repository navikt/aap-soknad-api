package no.nav.aap.api.søknad

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.util.LoggerUtil
import org.springframework.kafka.core.KafkaProducerException
import org.springframework.kafka.core.KafkaSendCallback
import org.springframework.kafka.support.SendResult

class SendCallback<K, V>(private val msg: String) : KafkaSendCallback<K, V> {
    private val log = LoggerUtil.getLogger(javaClass)

    override fun onSuccess(result: SendResult<K, V>?) =
        with(result) {
            log.info("Sendte $msg med id ${this?.producerRecord?.key()}   og offset ${this?.recordMetadata?.offset()} på ${this?.recordMetadata?.topic()}")
        }

    override fun onFailure(e: KafkaProducerException) =
        throw IntegrationException(msg = "Kunne ikke sende $msg", cause = e)
}