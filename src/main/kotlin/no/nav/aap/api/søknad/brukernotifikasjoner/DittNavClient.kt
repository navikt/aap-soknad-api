package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.SendCallback
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
import org.springframework.transaction.annotation.Transactional
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
                       eventId: UUID = UUID.randomUUID(),
                       fnr: Fødselsnummer,
                       tekst: String,
                       mellomlager: Boolean = false) =
        with(cfg.beskjed) {
            if (enabled) {
                dittNav.send(ProducerRecord(topic, key(type.name, eventId, fnr, "beskjed"), beskjed(type, tekst)))
                    .addCallback(SendCallback("opprett beskjed"))
                repos.beskjeder.save(JPADittNavBeskjed(fnr = fnr.fnr,
                        eventid = eventId,
                        mellomlager = mellomlager)).also {
                    log.trace(CONFIDENTIAL, "Opprettet beskjed i DB $it")
                }
                eventId
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
                val eventId = UUID.fromString(callId())
                with(key(type.name, eventId, fnr, "oppgave")) {
                    dittNav.send(ProducerRecord(topic, this, oppgave(type, tekst)))
                        .addCallback(SendCallback("opprett oppgave"))
                    repos.oppgaver.save(JPADittNavOppgave(fnr = fnr.fnr, eventid = eventId))
                        .also {
                            log.trace(CONFIDENTIAL, "Opprettet oppgave i DB $it")
                        }
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
                dittNav.send(ProducerRecord(done, key(type.name, eventId, fnr, "done"), done()))
                    .addCallback(SendCallback("avslutt oppgave"))
                repos.oppgaver.done(eventId)
            }
            else {
                log.info("Sender ikke done til Ditt Nav")
            }
        }

    @Transactional
    fun avsluttBeskjed(type: SkjemaType, fnr: Fødselsnummer, eventId: UUID) =
        with(cfg) {
            if (beskjed.enabled) {
                dittNav.send(ProducerRecord(done, key(type.name, eventId, fnr, "done"), done()))
                    .addCallback(SendCallback("avslutt beskjed"))
                repos.beskjeder.done(eventId)
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

    private fun key(grupperingId: String, eventId: UUID, fnr: Fødselsnummer, type: String) =
        with(cfg) {
            NokkelInputBuilder()
                .withFodselsnummer(fnr.fnr)
                .withEventId(eventId.toString())
                .withGrupperingsId(grupperingId)
                .withAppnavn(app)
                .withNamespace(namespace)
                .build().also {
                    log.info(CONFIDENTIAL, "Key for Ditt Nav $type er $it")
                }
        }

    fun eventIdForFnr(fnr: Fødselsnummer) = repos.beskjeder.eventIdForFnr(fnr.fnr)

}