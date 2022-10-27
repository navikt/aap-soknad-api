package no.nav.aap.api.søknad.minside

import io.micrometer.core.annotation.Counted
import io.micrometer.core.instrument.Metrics
import java.time.Duration
import java.time.LocalDateTime.now
import java.time.ZoneOffset.UTC
import java.util.*
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.UTLAND_SØKNAD
import no.nav.aap.api.søknad.SendCallback
import no.nav.aap.api.søknad.minside.MinSideBeskjedRepository.Beskjed
import no.nav.aap.api.søknad.minside.MinSideClient.NotifikasjonType.BESKJED
import no.nav.aap.api.søknad.minside.MinSideClient.NotifikasjonType.OPPGAVE
import no.nav.aap.api.søknad.minside.MinSideConfig.BacklinksConfig
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.MINAAPSTD
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.MinSideBacklinkContext.MINAAP
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.MinSideBacklinkContext.SØKNAD
import no.nav.aap.api.søknad.minside.MinSideOppgaveRepository.Oppgave
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
import org.springframework.web.util.UriComponentsBuilder.fromUri

@ConditionalOnGCP
class MinSideClient(private val minside: KafkaOperations<NokkelInput, Any>,
                    private val cfg: MinSideConfig,
                    private val repos: MinSideRepositories) {

    private val log = getLogger(javaClass)

    @Transactional
    @Counted(value = "soknad.beskjed.opprettet", description = "Antall beskjeder opprettet")
    fun opprettBeskjed(fnr: Fødselsnummer,
                       tekst: String,
                       eventId: UUID = callIdAsUUID(),
                       type: MinSideNotifikasjonType = MINAAPSTD,
                       eksternVarsling: Boolean = true) =
        with(cfg.beskjed) {
            if (enabled) {
                log.trace(CONFIDENTIAL, "Oppretter Min Side beskjed $tekst for $fnr, ekstern varsling $eksternVarsling og eventid $eventId")
                minside.send(ProducerRecord(topic, key(type.skjemaType, eventId, fnr),
                        beskjed(tekst, varighet,type, eksternVarsling)))
                    .addCallback(SendCallback("opprett beskjed med tekst $tekst, eventid $eventId og ekstern varsling $eksternVarsling"))
                repos.beskjeder.save(Beskjed(fnr.fnr, eventId, ekstern = eksternVarsling)).eventid
            }
            else {
                log.info("Oppretter ikke beskjed i Ditt Nav for $fnr")
                null
            }
        }

    @Transactional
    @Counted(value = "soknad.oppgave.opprettet", description = "Antall oppgaver opprettet")
    fun opprettOppgave(fnr: Fødselsnummer,
                       tekst: String,
                       eventId: UUID = callIdAsUUID(),
                       type: MinSideNotifikasjonType = MINAAPSTD,
                       eksternVarsling: Boolean = true) =
        with(cfg.oppgave) {
            if (enabled) {
                log.trace("Oppretter Min Side oppgave for $fnr, ekstern varsling $eksternVarsling og eventid $eventId")
                minside.send(ProducerRecord(topic, key(type.skjemaType, eventId, fnr),
                        oppgave(tekst, varighet, type, eventId, eksternVarsling)))
                    .addCallback(SendCallback("opprett oppgave med tekst $tekst, eventid $eventId og ekstern varsling $eksternVarsling"))
                repos.oppgaver.save(Oppgave(fnr.fnr, eventId, ekstern = eksternVarsling)).eventid
            }
            else {
                log.info("Oppretter ikke oppgave i Min Side for $fnr")
                null
            }
        }

    @Transactional
    @Counted(value = "soknad.oppgave.avsluttet", description = "Antall oppgaver avsluttet")
    fun avsluttOppgave(fnr: Fødselsnummer, eventId: UUID, type: SkjemaType = STANDARD) =
        with(cfg.oppgave) {
            if (enabled) {
                repos.oppgaver.findByFnrAndEventidAndDoneIsFalse(fnr.fnr, eventId)?.let {
                    avsluttMinSide(it.eventid, fnr, OPPGAVE, type)
                    it.done = true
                } ?: log.warn("Kunne ikke finne oppgave med eventid $eventId for fnr $fnr i DB, allerede avsluttet?. Avslutter på Min Side likevel").also {
                    avsluttMinSide(eventId, fnr, OPPGAVE, type)
                }
            }
            else {
                log.info("Sender ikke avslutt oppgave til Ditt Nav for $fnr")
            }
        }

    @Transactional
    @Counted(value = "soknad.beskjed.avsluttet", description = "Antall beskjeder avsluttet")
    fun avsluttBeskjed(fnr: Fødselsnummer, eventId: UUID, type: SkjemaType = STANDARD) =
        with(cfg.beskjed) {
            if (enabled) {
                repos.beskjeder.findByFnrAndEventidAndDoneIsFalse(fnr.fnr, eventId)?.let {
                    avsluttMinSide(it.eventid, fnr, BESKJED, type)
                    it.done = true

                } ?: log.warn("Kunne ikke avslutte beskjed med eventid $eventId for fnr $fnr i DB, allerede avsluttet?. Avslutter på Min Side likevel").also {
                    avsluttMinSide(eventId, fnr, BESKJED, type)
                }
            }
            else {
                log.info("Sender ikke avslutt beskjed til Min Side for beskjed for $fnr")
            }
        }

    private fun avsluttMinSide(eventId: UUID, fnr: Fødselsnummer, notifikasjonType: NotifikasjonType, type: SkjemaType = STANDARD) =
        minside.send(ProducerRecord(cfg.done, key(type,eventId, fnr), done()))
            .addCallback(SendCallback("avslutt $notifikasjonType med eventid $eventId")).also {
                when (notifikasjonType) {
                   OPPGAVE -> oppgaverAvsluttet.increment()
                   BESKJED -> beskjederAvsluttet.increment()
                }
            }


    private fun beskjed(tekst: String, varighet: Duration, type: MinSideNotifikasjonType, eksternVarsling: Boolean) =
        with(cfg.beskjed) {
            BeskjedInputBuilder()
                .withSikkerhetsnivaa(sikkerhetsnivaa)
                .withTidspunkt(now(UTC))
                .withSynligFremTil(now(UTC).plus(varighet))
                .withLink(type.link(cfg.backlinks)?.toURL())
                .withTekst(tekst)
                .withEksternVarsling(eksternVarsling)
                .withPrefererteKanaler(*preferertekanaler.toTypedArray())
                .build().also { m ->
                    log.trace(CONFIDENTIAL, "Melding ${m.tekst}, prefererte kanaler ${m.prefererteKanaler} og ekstern notifikasjon ${m.eksternVarsling}")
                }
        }

    private fun oppgave(tekst: String, varighet: Duration, type: MinSideNotifikasjonType, eventId: UUID, eksternVarsling: Boolean) =
        with(cfg.oppgave) {
            OppgaveInputBuilder()
                .withSikkerhetsnivaa(sikkerhetsnivaa)
                .withTidspunkt(now(UTC))
                .withSynligFremTil(now(UTC).plus(varighet))
                .withLink(type.link(cfg.backlinks, eventId)?.toURL())
                .withTekst(tekst)
                .withEksternVarsling(eksternVarsling)
                .withPrefererteKanaler(*preferertekanaler.toTypedArray())
                .build().also { o ->
                    log.trace(CONFIDENTIAL, "Oppgave  ${o.tekst}, prefererte kanaler ${o.prefererteKanaler} og ekstern notifikasjon ${o.eksternVarsling}")
                }
        }

    private fun done() = DoneInputBuilder().withTidspunkt(now(UTC)).build()

    private fun key(type: SkjemaType, eventId: UUID, fnr: Fødselsnummer) =
        with(cfg) {
            NokkelInputBuilder()
                .withFodselsnummer(fnr.fnr)
                .withEventId("$eventId")
                .withGrupperingsId(type.name)
                .withAppnavn(app)
                .withNamespace(namespace)
                .build().also {
                    log.info(CONFIDENTIAL, "Key for Ditt Nav $type er $it")
                }
        }

    private enum class NotifikasjonType  {
        OPPGAVE,BESKJED
    }
    companion object {
        private val oppgaverAvsluttet = Metrics.counter("soknad.oppgave.avsluttet")
        private val beskjederAvsluttet = Metrics.counter("soknad.beskjed.avsluttet")
    }
}

data class MinSideNotifikasjonType private constructor(val skjemaType: SkjemaType,
                                                       private val ctx: MinSideBacklinkContext) {

    private enum class MinSideBacklinkContext {
        MINAAP,
        SØKNAD
    }

    fun link(cfg: BacklinksConfig, eventId: UUID? = null) =
        when (skjemaType) {
            STANDARD -> when (ctx) {
                MINAAP -> eventId?.let { fromUri(cfg.innsyn).queryParam("eventId", it).build().toUri() }
                    ?: cfg.innsyn

                SØKNAD -> cfg.standard
            }

            UTLAND_SØKNAD -> when (ctx) {
                MINAAP -> cfg.innsyn
                SØKNAD -> cfg.utland
            }

            else -> null
        }

    companion object {
        val MINAAPSTD = MinSideNotifikasjonType(STANDARD, MINAAP)
        val SØKNADSTD = MinSideNotifikasjonType(STANDARD, SØKNAD)
    }
}