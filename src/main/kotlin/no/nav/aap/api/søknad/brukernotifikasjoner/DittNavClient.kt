package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.søknad.SendCallback
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavBeskjedRepository.Beskjed
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavConfig.BacklinksConfig
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavNotifikasjonType.DittNavBacklinkContext.MINAAP
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavNotifikasjonType.DittNavBacklinkContext.SØKNAD
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavOppgaveRepository.Oppgave
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.callIdAsUUID
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
    fun opprettBeskjed(type: DittNavNotifikasjonType,
                       eventId: UUID,
                       fnr: Fødselsnummer,
                       tekst: String,
                       mellomlager: Boolean = false) =
        with(cfg.beskjed) {
            if (enabled) {
                log.trace("Oppretter Ditt Nav beskjed for $fnr og eventid $eventId")
                dittNav.send(ProducerRecord(topic,
                        key(type.skjemaType.name, eventId, fnr, "beskjed"), beskjed("$tekst ($eventId)", type)))
                    .addCallback(SendCallback("opprett beskjed med eventid $eventId"))
                log.trace("Oppretter Ditt Nav beskjed i DB")
                repos.beskjeder.save(Beskjed(fnr = fnr.fnr,
                        eventid = eventId,
                        mellomlager = mellomlager)).also {
                    log.trace(CONFIDENTIAL, "Opprettet Ditt Nav beskjed $it i DB")
                }.eventid
            }
            else {
                log.info("Sender ikke opprett beskjed til Ditt Nav for $fnr")
                null
            }
        }

    @Transactional
    fun opprettOppgave(type: DittNavNotifikasjonType, fnr: Fødselsnummer, tekst: String) =
        with(cfg.oppgave) {
            if (enabled) {
                val eventId = callIdAsUUID()
                log.trace("Oppretter Ditt Nav oppgave for $fnr og eventid $eventId")
                with(key(type.skjemaType.name, eventId, fnr, "oppgave")) {
                    dittNav.send(ProducerRecord(topic, this, oppgave(tekst, type)))
                        .addCallback(SendCallback("opprett oppgave med eventid $eventId"))
                    repos.oppgaver.save(Oppgave(fnr = fnr.fnr, eventid = eventId)).also {
                        log.trace(CONFIDENTIAL, "Opprettet oppgave $it i DB")
                    }.eventid
                }
            }
            else {
                log.info("Sender ikke opprett oppgave til Ditt Nav for $fnr")
                null
            }
        }

    @Transactional
    fun avsluttOppgave(type: SkjemaType, fnr: Fødselsnummer, eventId: UUID) =
        with(cfg) {
            if (oppgave.enabled) {
                dittNav.send(ProducerRecord(done, key(type.name, eventId, fnr, "done"), done()))
                    .addCallback(SendCallback("avslutt oppgave med eventid $eventId"))
                log.trace("Setter oppgave done i DB for eventId $eventId")
                when (val rows = repos.oppgaver.done(eventId)) {
                    0 -> log.warn("Kunne ikke sette oppgave med eventid $eventId for $fnr til done i DB, ingen rader funnet")
                    1 -> log.trace("Satt oppgave med eventid $eventId for $fnr done i DB")
                    else -> log.warn("Satte et uventet antall rader ($rows) til oppdatert for oppgave med eventid  $eventId og $fnr til done i DB")
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
                    .addCallback(SendCallback("avslutt beskjed med eventid $eventId"))
                log.trace("Setter beskjed done i DB for eventid $eventId")
                when (val rows = repos.beskjeder.done(eventId)) {
                    0 -> log.warn("Kunne ikke sette beskjed med eventid $eventId for fnr $fnr til done i DB, ingen rader oppdatert")
                    1 -> log.trace("Satt beskjed med eventid $eventId for $fnr done i DB")
                    else -> log.warn("Satte et uventet antall rader ($rows) til oppdatert for beskjed med eventid $eventId og fnr $fnr til done i DB")
                }
            }
            else {
                log.info("Sender ikke avslutt beskjed til Ditt Nav for beskjed for $fnr")
            }
        }

    @Transactional(readOnly = true)
    fun eventIdsForFnr(fnr: Fødselsnummer) =
        repos.beskjeder.eventIdForFnr(fnr.fnr)
            .also {
                when (val size = it.size) {
                    0 -> log.warn("Fant ingen eventid i DB for $fnr")
                    1 -> log.trace("Fant som forventet en rad med eventid ${it.first()} for $fnr")
                    else -> log.warn("Fant et uventet antall rader ($size) med eventids $it for $fnr, dette bør undersøkes nærmere")
                }
            }

    private fun beskjed(tekst: String, type: DittNavNotifikasjonType) =
        with(cfg.beskjed) {
            BeskjedInputBuilder()
                .withSikkerhetsnivaa(sikkerhetsnivaa)
                .withTidspunkt(now(UTC))
                .withSynligFremTil(now(UTC).plus(varighet))
                .withLink(type.link(cfg.backlinks))
                .withTekst(tekst)
                .withEksternVarsling(eksternVarsling)
                .withPrefererteKanaler(*preferertekanaler.toTypedArray())
                .build()
        }

    private fun oppgave(tekst: String, type: DittNavNotifikasjonType) =
        with(cfg.oppgave) {
            OppgaveInputBuilder()
                .withSikkerhetsnivaa(sikkerhetsnivaa)
                .withTidspunkt(now(UTC))
                .withSynligFremTil(now(UTC).plus(varighet))
                .withLink(type.link(cfg.backlinks))
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

data class DittNavNotifikasjonType private constructor(val skjemaType: SkjemaType, val ctx: DittNavBacklinkContext) {

    enum class DittNavBacklinkContext {
        MINAAP,
        SØKNAD
    }

    fun link(cfg: BacklinksConfig) =
        when (skjemaType) {
            STANDARD -> when (ctx) {
                MINAAP -> cfg.innsyn
                else -> cfg.standard
            }

            UTLAND -> when (ctx) {
                MINAAP -> cfg.innsyn
                else -> cfg.utland
            }
        }

    companion object {
        val MINAAPSTD = DittNavNotifikasjonType(STANDARD, MINAAP)
        val MINAAPUTLAND = DittNavNotifikasjonType(UTLAND, MINAAP)
        val SØKNADSTD = DittNavNotifikasjonType(STANDARD, SØKNAD)
        val SØKNADUTLAND = DittNavNotifikasjonType(UTLAND, SØKNAD)

    }
}