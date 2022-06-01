package no.nav.aap.api.søknad.dittnav

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.søknad.dittnav.DittNavConfig.TopicConfig
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.DoneInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.OppgaveInputBuilder
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback
import org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri
import java.time.LocalDateTime.now
import java.time.ZoneOffset.UTC
import java.util.*

@Service
@ConditionalOnGCP
class DittNavRouter(private val dittNav: KafkaOperations<NokkelInput, Any>,
                    private val cfg: DittNavConfig,
                    @Value("\${nais.app.name}") private val app: String,
                    @Value("\${nais.namespace}") private val namespace: String,
                    private val beskjedRepo: JPADittNavBeskjedRepository,
                    private val oppgaveRepo: JPADittNavOppgaveRepository) {

    private val log = LoggerUtil.getLogger(javaClass)

    fun opprettBeskjed(fnr: Fødselsnummer, type: SkjemaType) =
        if (cfg.beskjed.enabled) {
            with(nøkkelInput(fnr, type.name, UUID.randomUUID().toString())) {
                log.info(CONFIDENTIAL, "Sender beskjed til Ditt Nav med key $this")
                dittNav.send(ProducerRecord(cfg.beskjed.topic,
                        this,
                        beskjed(cfg.beskjed, type, "Mottatt ${type.tittel}")))
                    .addCallback(DittNavBeskjedCallback(this, beskjedRepo))
            }
        }
        else {
            log.info("Sender ikke beskjed til Ditt Nav")
        }

    fun opprettOppgave(fnr: Fødselsnummer, type: SkjemaType, tekst: String) =
        if (cfg.oppgave.enabled) {
            with(nøkkelInput(fnr, type.name, UUID.randomUUID().toString())) {
                log.info(CONFIDENTIAL, "Sender oppgave til Ditt Nav med key $this")
                dittNav.send(ProducerRecord(cfg.oppgave.topic, this, oppgave(cfg.oppgave, type, tekst)))
                    .addCallback(DittNavOppgaveCallback(this, oppgaveRepo))
            }
        }
        else {
            log.info("Sender ikke oppgave til Ditt Nav")
        }

    fun done(fnr: Fødselsnummer, type: SkjemaType, eventId: String) =
        if (cfg.done.enabled) {
            with(nøkkelInput(fnr, type.name, eventId)) {
                log.info(CONFIDENTIAL, "Sender done til Ditt Nav med key $this")
                dittNav.send(ProducerRecord(cfg.done.topic, this, done()))
                    .addCallback(DittNavDoneCallback(this, oppgaveRepo))
            }
        }
        else {
            log.info("Sender ikke done til Ditt Nav")
        }

    private fun beskjed(cfg: TopicConfig, type: SkjemaType, tekst: String) =
        BeskjedInputBuilder()
            .withSikkerhetsnivaa(3)
            .withTidspunkt(now(UTC))
            .withSynligFremTil(now(UTC).plus(cfg.varighet))
            .withLink(replaceWith("/aap/${type.name}"))
            .withTekst(tekst)
            .build()

    private fun oppgave(cfg: TopicConfig, type: SkjemaType, tekst: String) =
        OppgaveInputBuilder()
            .withSikkerhetsnivaa(3)
            .withTidspunkt(now(UTC))
            .withSynligFremTil(now(UTC).plus(cfg.varighet))
            .withLink(replaceWith("/aap/${type.name}"))
            .withTekst(tekst)
            .build()

    private fun done() =
        DoneInputBuilder()
            .withTidspunkt(now(UTC))
            .build()

    private fun replaceWith(replacement: String) =
        fromCurrentRequestUri().replacePath(replacement).build().toUri().toURL()

    private fun nøkkelInput(fnr: Fødselsnummer, grupperingId: String, eventId: String) = NokkelInputBuilder()
        .withFodselsnummer(fnr.fnr)
        .withEventId(eventId)
        .withGrupperingsId(grupperingId)
        .withAppnavn(app)
        .withNamespace(namespace)
        .build()

    private class DittNavBeskjedCallback(private val key: NokkelInput,
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

    private class DittNavOppgaveCallback(private val key: NokkelInput,
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

    private class DittNavDoneCallback(private val key: NokkelInput,
                                      private val oppgaveRepo: JPADittNavOppgaveRepository) :
        ListenableFutureCallback<SendResult<NokkelInput, Any>?> {
        private val log = LoggerUtil.getLogger(javaClass)

        override fun onSuccess(result: SendResult<NokkelInput, Any>?) {
            log.info("Sendte done til Ditt Nav  med id ${key.getEventId()} og offset ${result?.recordMetadata?.offset()} på ${result?.recordMetadata?.topic()}")
            oppgaveRepo.findByRef(key.getEventId())?.let {
                it.done = now()
                oppgaveRepo.save(it)
                also {
                    log.info("Oppdatert done timestamp på oppgave i Ditt Nav i DB med ref ${key.getEventId()}")
                }
            } ?: log.info("Kunne ikke oppdatere innslag med eventId ${key.getEventId()}")
        }

        override fun onFailure(e: Throwable) {
            log.warn("Kunne ikke sende done til Ditt Nav med id ${key.getEventId()}", e)
        }
    }
}