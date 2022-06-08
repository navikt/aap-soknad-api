package no.nav.aap.api.søknad.dittnav

import no.nav.aap.util.LoggerUtil
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFutureCallback

class DittNavCallbacks {
    class DittNavBeskjedCallback(private val key: NokkelInput,
                                 private val beskjedRepo: JPADittNavBeskjedRepository) :
        ListenableFutureCallback<SendResult<NokkelInput, Any>?> {
        private val log = LoggerUtil.getLogger(javaClass)

        override fun onSuccess(result: SendResult<NokkelInput, Any>?) {
            log.info("Sendte beskjed til Ditt Nav  med id ${key.getEventId()} og offset ${result?.recordMetadata?.offset()} på ${result?.recordMetadata?.topic()}")
            beskjedRepo.save(JPADittNavMelding(key.getFodselsnummer(), ref = key.getEventId()))
                .also {
                    log.info("Lagret info om beskjed til Ditt Nav i DB med id ${it.id}")
                }
        }

        override fun onFailure(e: Throwable) {
            log.warn("Kunne ikke sende beskjed til Ditt Nav med id ${key.getEventId()}", e)
        }
    }

    class DittNavOppgaveCallback(private val key: NokkelInput,
                                 private val oppgaveRepo: JPADittNavOppgaveRepository) :
        ListenableFutureCallback<SendResult<NokkelInput, Any>?> {
        private val log = LoggerUtil.getLogger(javaClass)

        override fun onSuccess(result: SendResult<NokkelInput, Any>?) {
            log.info("Sendte oppgave til Ditt Nav  med id ${key.getEventId()} og offset ${result?.recordMetadata?.offset()} på ${result?.recordMetadata?.topic()}")
            oppgaveRepo.save(JPADittNavOppgave(key.getFodselsnummer(), ref = key.getEventId()))
                .also {
                    log.info("Lagret info om oppgave til Ditt Nav i DB med id ${it.id}")
                }
        }

        override fun onFailure(e: Throwable) {
            log.warn("Kunne ikke sende oppgave til Ditt Nav med id ${key.getEventId()}", e)
        }
    }

    class DittNavDoneCallback(private val key: NokkelInput,
                              private val oppgaveRepo: JPADittNavOppgaveRepository) :
        ListenableFutureCallback<SendResult<NokkelInput, Any>?> {
        private val log = LoggerUtil.getLogger(javaClass)

        override fun onSuccess(result: SendResult<NokkelInput, Any>?) {
            log.info("Sendte done til Ditt Nav  med id ${key.getEventId()} og offset ${result?.recordMetadata?.offset()} på ${result?.recordMetadata?.topic()}")
            oppgaveRepo.done(key.getEventId())
        }

        override fun onFailure(e: Throwable) {
            log.warn("Kunne ikke sende done til Ditt Nav med id ${key.getEventId()}", e)
        }
    }
}