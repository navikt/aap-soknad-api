package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.springframework.kafka.support.SendResult
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.concurrent.ListenableFutureCallback

class DittNavCallbacks {
    class DittNavBeskjedCallback(private val key: NokkelInput) :
        ListenableFutureCallback<SendResult<NokkelInput, Any>?> {
        private val log = getLogger(javaClass)

        override fun onSuccess(result: SendResult<NokkelInput, Any>?) =
            log.info("Sendte en beskjed til Ditt Nav  med id ${key.eventId} og offset ${result?.recordMetadata?.offset()} på ${result?.recordMetadata?.topic()}")

        override fun onFailure(e: Throwable) {
            log.warn("Kunne ikke sende beskjed til Ditt Nav med id ${key.eventId}", e)
        }
    }

    class DittNavOppgaveCallback(private val key: NokkelInput) :
        ListenableFutureCallback<SendResult<NokkelInput, Any>?> {
        private val log = getLogger(javaClass)

        override fun onSuccess(result: SendResult<NokkelInput, Any>?) =
            log.info("Sendte oppgave til Ditt Nav  med id ${key.eventId} og offset ${result?.recordMetadata?.offset()} på ${result?.recordMetadata?.topic()}")

        override fun onFailure(e: Throwable) =
            log.warn("Kunne ikke sende oppgave til Ditt Nav med id ${key.eventId}", e)

    }

    class DittNavOppgaveDoneCallback(private val key: NokkelInput,
                                     private val repo: JPADittNavOppgaveRepository) :
        ListenableFutureCallback<SendResult<NokkelInput, Any>?> {
        private val log = getLogger(javaClass)

        override fun onSuccess(result: SendResult<NokkelInput, Any>?) =
            repo.done(key.eventId).also {
                log.info("Sendte done til Ditt Nav  med id ${key.eventId} og offset ${result?.recordMetadata?.offset()} på ${result?.recordMetadata?.topic()}")
            }

        override fun onFailure(e: Throwable) =
            log.warn("Kunne ikke sende done til Ditt Nav med id ${key.eventId}", e)
    }

    open class DittNavBeskjedDoneCallback(private val key: NokkelInput, private val repo: JPADittNavBeskjedRepository) :
        ListenableFutureCallback<SendResult<NokkelInput, Any>?> {
        private val log = getLogger(javaClass)

        @Transactional
        override fun onSuccess(result: SendResult<NokkelInput, Any>?) =
            repo.done(key.eventId).also {
                log.info("Sendte done til Ditt Nav  med id ${key.eventId} og offset ${result?.recordMetadata?.offset()} på ${result?.recordMetadata?.topic()}")
            }

        override fun onFailure(e: Throwable) =
            log.warn("Kunne ikke sende done til Ditt Nav med id ${key.eventId}", e)
    }
}