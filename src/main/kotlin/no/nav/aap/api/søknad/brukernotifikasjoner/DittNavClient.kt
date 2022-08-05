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
                log.trace("Oppretter Ditt Nav beskjed for $fnr og $eventId")
                dittNav.send(ProducerRecord(topic, key(type.name, eventId, fnr, "beskjed"), beskjed(type, tekst)))
                    .addCallback(SendCallback("opprett beskjed"))
                log.trace("Oppretter Ditt Nav beskjed i DB")
                repos.beskjeder.save(JPADittNavBeskjed(fnr = fnr.fnr, eventid = eventId, mellomlager = mellomlager))
                    .also {
                        log.trace("Opprettet Ditt Nav beskjed $it i DB")
                    }
                eventId
            }
            else {
                log.info("Sender ikke opprett beskjed til Ditt Nav for $fnr")
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
                            log.trace("Opprettet oppgave $it i DB")
                        }
                    eventId
                }
            }
            else {
                log.info("Sender ikke opprett oppgave til Ditt Nav for $fnr")
                callId()
            }
        }

    @Transactional
    fun avsluttOppgave(type: SkjemaType, fnr: Fødselsnummer, eventId: UUID) =
        with(cfg) {
            if (oppgave.enabled) {
                dittNav.send(ProducerRecord(done, key(type.name, eventId, fnr, "done"), done()))
                    .addCallback(SendCallback("avslutt oppgave"))
                log.trace("Setter oppgave done i DB for eventId $eventId")
                when (val rows = repos.oppgaver.done(eventId)) {
                    0 -> log.warn("Kunne ikke sette oppgave $eventId for $fnr til done i DB, ingen rader funnet")
                    1 -> log.trace("Satt oppgave $eventId for $fnr done i DB")
                    else -> log.warn("Satte et uventet antall rader ($rows) til oppdatert for oppgave $eventId og $fnr til done i DB")
                }
            }
            else {
                log.info("Sender ikke avslutt oppgave til Ditt Nav for $fnr")
            }
        }

    @Transactional
    fun avsluttBeskjed(type: SkjemaType, fnr: Fødselsnummer, eventId: UUID) =
        with(cfg) {
            if (beskjed.enabled) {
                dittNav.send(ProducerRecord(done, key(type.name, eventId, fnr, "done"), done()))
                    .addCallback(SendCallback("avslutt beskjed"))
                log.trace("Setter beskjed done i DB for  $eventId")
                when (val rows = repos.beskjeder.done(eventId)) {
                    0 -> log.warn("Kunne ikke sette beskjed $eventId for fnr $fnr til done i DB, ingen rader funnet")
                    1 -> log.trace("Satt beskjed $eventId for $fnr done i DB")
                    else -> log.warn("Satte et uventet antall rader ($rows) til oppdatert for beskjed $eventId og fnr $fnr til done i DB")
                }
            }
            else {
                log.info("Sender ikke avslutt beskjed til Ditt Nav for beskjed for $fnr")
            }
        }

    fun eventIdsForFnr(fnr: Fødselsnummer) =
        repos.beskjeder.eventIdForFnr(fnr.fnr)
            .also {
                when (val size = it.size) {
                    0 -> log.warn("Fant ingen eventId for $fnr")
                    1 -> log.trace("Fant som forventet en rad med eventId for $fnr")
                    else -> log.warn("Fant et uventet antall rader ($size) med eventIds for $fnr, bør undersøkes nærmere")
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
}