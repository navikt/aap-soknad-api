package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.callId
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.DoneInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.OppgaveInputBuilder
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.core.KafkaProducerException
import org.springframework.kafka.core.KafkaSendCallback
import org.springframework.kafka.support.SendResult
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri
import java.time.LocalDateTime.now
import java.time.ZoneOffset.UTC
import java.util.*

@ConditionalOnGCP
class DittNavClient(private val dittNav: KafkaOperations<NokkelInput, Any>,
                    private val cfg: DittNavConfig,
                    private val repos: DittNavRepositories) {

    private val log = getLogger(javaClass)

    @Transactional
    fun opprettBeskjed(type: SkjemaType = STANDARD,
                       eventId: UUID,
                       fnr: Fødselsnummer,
                       tekst: String,
                       mellomlager: Boolean = false) =
        with(cfg.beskjed) {
            if (enabled) {
                with(nøkkel(type.name, eventId, fnr, "beskjed")) {
                    dittNav.send(ProducerRecord(topic, this, beskjed(type, tekst)))
                        .addCallback(DittNavSendCallback("opprett beskjed"))
                    repos.beskjeder.save(JPADittNavBeskjed(fnr = fnr.fnr,
                            eventid = eventId,
                            mellomlager = mellomlager)).also { log.trace("Opprettet beskjed i DB $it") }

                    eventId
                }
            }
            else {
                log.info("Sender ikke opprett beskjed til Ditt Nav")
                callId()
            }
        }

    @Transactional
    fun opprettOppgave(type: SkjemaType, fnr: Fødselsnummer, tekst: String) =
        with(cfg.oppgave) {
            if (enabled) {
                with(nøkkel(type.name, UUID.fromString(callId()), fnr, "oppgave")) {
                    dittNav.send(ProducerRecord(topic, this, oppgave(type, tekst)))
                        .addCallback(DittNavSendCallback("opprett oppgave"))
                    repos.oppgaver.save(JPADittNavOppgave(fnr = fnr.fnr, eventid = UUID.fromString(eventId)))
                        .also { log.trace("Opprettet oppgave i DB $it") }
                    eventId
                }
            }
            else {
                log.info("Sender ikke opprett oppgave til Ditt Nav")
                callId()
            }
        }

    @Transactional
    fun avsluttOppgave(type: SkjemaType, fnr: Fødselsnummer, eventId: UUID) =
        with(cfg) {
            if (oppgave.enabled) {
                with(nøkkel(type.name, eventId, fnr, "done")) {
                    dittNav.send(ProducerRecord(done.topic, this, done()))
                        .addCallback(DittNavSendCallback("avslutt oppgave"))
                    repos.oppgaver.done(eventId)
                }
            }
            else {
                log.info("Sender ikke done til Ditt Nav")
            }
        }

    @Transactional
    fun avsluttBeskjed(type: SkjemaType, fnr: Fødselsnummer, eventId: UUID) =
        with(cfg) {
            if (beskjed.enabled) {
                with(nøkkel(type.name, eventId, fnr, "done")) {
                    dittNav.send(ProducerRecord(done.topic, this, done()))
                        .addCallback(DittNavSendCallback("avslutt beskjed"))
                    repos.beskjeder.done(eventId)
                }
            }
            else {
                log.info("Sender ikke done til Ditt Nav for beskjed")
            }
        }

    private fun beskjed(type: SkjemaType, tekst: String) =
        with(cfg.beskjed) {
            BeskjedInputBuilder()
                .withSikkerhetsnivaa(sikkerhetsnivaa)
                .withTidspunkt(now(UTC))
                .withSynligFremTil(now(UTC).plus(varighet))
                //.withLink(replaceWith("/aap/${type.name}"))  TODO
                .withTekst(tekst)
                .withEksternVarsling(eksternVarsling)
                .withPrefererteKanaler(*preferertekanaler.toTypedArray())
                .build()
        }

    private fun oppgave(type: SkjemaType, tekst: String) =
        with(cfg.oppgave) {
            OppgaveInputBuilder()
                .withSikkerhetsnivaa(sikkerhetsnivaa)
                .withTidspunkt(now(UTC))
                .withSynligFremTil(now(UTC).plus(varighet))
                //  .withLink(replaceWith("/aap/${type.name}")) TODO
                .withTekst(tekst)
                .withEksternVarsling(eksternVarsling)
                .withPrefererteKanaler(*preferertekanaler.toTypedArray())
                .build()
        }

    private fun done() =
        DoneInputBuilder()
            .withTidspunkt(now(UTC))
            .build()

    private fun replaceWith(replacement: String) =
        fromCurrentRequestUri().replacePath(replacement).build().toUri().toURL()

    private fun nøkkel(grupperingId: String, eventId: UUID, fnr: Fødselsnummer, type: String) =
        with(cfg) {
            NokkelInputBuilder()
                .withFodselsnummer(fnr.fnr)
                .withEventId(eventId.toString())
                .withGrupperingsId(grupperingId)
                .withAppnavn(app)
                .withNamespace(namespace)
                .build().also { log.info(CONFIDENTIAL, "Key for Ditt Nav $type er $it") }
        }

    fun eventIdForFnr(fnr: Fødselsnummer) = repos.beskjeder.getMellomlagretEventIdForFnr(fnr)

    private class DittNavSendCallback(private val msg: String) : KafkaSendCallback<NokkelInput, Any> {
        private val log = getLogger(javaClass)

        override fun onSuccess(result: SendResult<NokkelInput, Any>?) =
            with(result) {
                log.info("Sendte $msg til Ditt Nav med id ${this?.producerRecord?.key()?.eventId}   og offset ${this?.recordMetadata?.offset()} på ${this?.recordMetadata?.topic()}")
            }

        override fun onFailure(e: KafkaProducerException) =
            throw IntegrationException(msg = "Kunne ikke sende $msg til Ditt Nav", cause = e)
    }
}