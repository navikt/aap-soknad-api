package no.nav.aap.api.søknad.minside

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.søknad.SendCallback
import no.nav.aap.api.søknad.minside.MinSideBeskjedRepository.Beskjed
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
import java.time.LocalDateTime.now
import java.time.ZoneOffset.UTC
import java.util.*

@ConditionalOnGCP
class MinSideClient(private val minside: KafkaOperations<NokkelInput, Any>,
                    private val cfg: MinSideConfig,
                    private val repos: MinSideRepositories) {

    private val log = getLogger(javaClass)

    @Transactional
    fun opprettBeskjed(fnr: Fødselsnummer, tekst: String, eventId: UUID = callIdAsUUID(),
                       type: MinSideNotifikasjonType = MINAAPSTD, eksternVarsling: Boolean = true) =
        with(cfg.beskjed) {
            if (enabled) {
                log.trace(CONFIDENTIAL, "Oppretter Min Side beskjed $tekst for $fnr, ekstern varsling $eksternVarsling og eventid $eventId")
                minside.send(ProducerRecord(topic,
                        key(type.skjemaType, eventId, fnr),
                        beskjed(tekst, type, eksternVarsling)))
                    .addCallback(SendCallback("opprett beskjed med eventid $eventId"))
                repos.beskjeder.save(Beskjed(fnr.fnr, eventId)).eventid
            }
            else {
                log.info("Sender ikke opprett beskjed til Ditt Nav for $fnr")
                null
            }
        }

    @Transactional
    fun opprettOppgave(fnr: Fødselsnummer,
                       eventId: UUID = callIdAsUUID(),
                       tekst: String,
                       type: MinSideNotifikasjonType = MINAAPSTD,
                       eksternVarsling: Boolean = true) =
        with(cfg.oppgave) {
            if (enabled) {
                log.trace("Oppretter Min Side oppgave for $fnr, ekstern varsling $eksternVarsling og eventid $eventId")
                minside.send(ProducerRecord(topic,
                        key(type.skjemaType, eventId, fnr),
                        oppgave(tekst, type, eventId, eksternVarsling)))
                    .addCallback(SendCallback("opprett oppgave med eventid $eventId"))
                repos.oppgaver.save(Oppgave(fnr.fnr, eventId)).eventid
            }
            else {
                log.info("Sender ikke opprett oppgave til Min Side for $fnr")
                null
            }
        }

    @Transactional
    fun avsluttOppgave(fnr: Fødselsnummer, eventId: UUID, type: SkjemaType = STANDARD) =
        with(cfg) {
            if (oppgave.enabled) {
                repos.oppgaver.findByFnrAndEventid(fnr.fnr, eventId)?.let {
                    if (!it.done) {
                        minside.send(ProducerRecord(done, key(type, eventId, fnr), done()))
                            .addCallback(SendCallback("avslutt oppgave med eventid $eventId"))
                        it.done = true
                    }
                    else {
                        log.trace("Oppgave $eventId er allerede avsluttet")
                    }
                } ?: log.warn("Kunne ikke avslutte oppgave med eventid $eventId for fnr $fnr i DB")
            }
            else {
                log.info("Sender ikke avslutt oppgave til Ditt Nav for $fnr")
            }
        }

    @Transactional
    fun avsluttBeskjed(type: SkjemaType, fnr: Fødselsnummer, eventId: UUID) =
        with(cfg) {
            if (beskjed.enabled) {
                repos.beskjeder.findByFnrAndEventid(fnr.fnr, eventId)?.let {
                    if (!it.done) {
                        minside.send(ProducerRecord(done, key(type, eventId, fnr), done()))
                            .addCallback(SendCallback("avslutt beskjed med eventid $eventId"))
                        it.done = true
                    }
                    else {
                        log.trace("Beskjed $eventId er allerede avsluttet")
                    }

                } ?: log.warn("Kunne ikke avslutte beskjed med eventid $eventId for fnr $fnr i DB")
            }
            else {
                log.info("Sender ikke avslutt beskjed til Min Side for beskjed for $fnr")
            }
        }

    private fun beskjed(tekst: String, type: MinSideNotifikasjonType, eksternVarsling: Boolean) =
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
                    log.trace(CONFIDENTIAL,
                            "Melding ${m.tekst}, prefererte kanaler ${m.prefererteKanaler} og ekstern notifikasjon ${m.eksternVarsling}")
                }
        }

    private fun oppgave(tekst: String, type: MinSideNotifikasjonType, eventId: UUID, eksternVarsling: Boolean) =
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
                    log.trace(CONFIDENTIAL,
                            "Oppgave  ${o.tekst}, prefererte kanaler ${o.prefererteKanaler} og ekstern notifikasjon ${o.eksternVarsling}")
                }
        }

    private fun done() =
        DoneInputBuilder()
            .withTidspunkt(now(UTC))
            .build()

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

            UTLAND -> when (ctx) {
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