package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.springframework.kafka.core.KafkaProducerException
import org.springframework.kafka.core.KafkaSendCallback
import org.springframework.kafka.support.SendResult

class DittNavSendCallback(private val msg: String) : KafkaSendCallback<NokkelInput, Any> {
    private val log = getLogger(javaClass)

    override fun onSuccess(result: SendResult<NokkelInput, Any>?) =
        with(result) {
            log.info("Sendte $msg til Ditt Nav med id ${this?.producerRecord?.key()?.eventId}   og offset ${this?.recordMetadata?.offset()} på ${this?.recordMetadata?.topic()}")
        }

    override fun onFailure(e: KafkaProducerException) =
        throw IntegrationException(msg = "Kunne ikke sende $msg til Ditt Nav", cause = e)
}