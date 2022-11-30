package no.nav.aap.api.søknad.minside

import io.micrometer.core.annotation.Counted
import io.micrometer.core.instrument.Metrics
import java.time.Duration
import java.time.LocalDateTime.now
import java.time.ZoneOffset.UTC
import java.util.*
import no.nav.aap.api.config.Metrikker.AVSLUTTET_BESKJED
import no.nav.aap.api.config.Metrikker.AVSLUTTET_OPPGAVE
import no.nav.aap.api.config.Metrikker.OPPRETTET_BESKJED
import no.nav.aap.api.config.Metrikker.OPPRETTET_OPPGAVE
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.UTLAND_SØKNAD
import no.nav.aap.api.felles.error.IntegrationException
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
import no.nav.aap.util.StringExtensions.partialMask
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.DoneInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.OppgaveInputBuilder
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.kafka.core.KafkaProducerException
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder.fromUri

@ConditionalOnGCP
class MinSideClient(private val minside: KafkaTemplate<NokkelInput, Any>,
                    private val cfg: MinSideConfig,
                    private val repos: MinSideRepositories) {

    private val log = getLogger(javaClass)

    @Transactional
    @Counted(value = OPPRETTET_BESKJED, description = "Antall beskjeder opprettet")
    fun opprettBeskjed(fnr: Fødselsnummer,
                       tekst: String,
                       eventId: UUID = callIdAsUUID(),
                       mellomlagring: Boolean = false,
                       type: MinSideNotifikasjonType = MINAAPSTD,
                       eksternVarsling: Boolean = true) =
        with(cfg.beskjed) {
            if (enabled) {
                log.trace("Oppretter Min Side beskjed med ekstern varsling $eksternVarsling og eventid $eventId")
                minside.send(ProducerRecord(topic, key(type.skjemaType, eventId, fnr), beskjed("$tekst", varighet,type, eksternVarsling)))
                    .whenComplete { res, e ->
                        e?.let {
                            throw IntegrationException(msg = "Kunne ikke opprtte beskjed i Min Side", cause = it as KafkaProducerException)
                        } ?: run {
                            with(res) {
                                log.trace("Sendte opprett beskjed med tekst $tekst, eventid $eventId og ekstern varsling $eksternVarsling på offset ${recordMetadata.offset()} partition${recordMetadata.partition()}på topic ${recordMetadata.topic()}")
                            }
                            repos.beskjeder.save(Beskjed(fnr.fnr, eventId, mellomlagring = mellomlagring,ekstern = eksternVarsling)).eventid
                        }
                    }
            }
            else {
                log.trace("Oppretter ikke beskjed i Ditt Nav for $fnr")
                null
            }
        }

    @Transactional
    @Counted(value = OPPRETTET_OPPGAVE, description = "Antall oppgaver opprettet")
    fun opprettOppgave(fnr: Fødselsnummer,
                       tekst: String,
                       eventId: UUID = callIdAsUUID(),
                       type: MinSideNotifikasjonType = MINAAPSTD,
                       eksternVarsling: Boolean = true) =
        with(cfg.oppgave) {
            if (enabled) {
                log.info("Oppretter Min Side oppgave med ekstern varsling $eksternVarsling og eventid $eventId")
                minside.send(ProducerRecord(topic, key(type.skjemaType, eventId, fnr),
                        oppgave(tekst, varighet, type, eventId, eksternVarsling)))
                    .whenComplete { res, e ->
                        e?.let {
                            throw IntegrationException(msg = "Kunne ikke opprette oppgave", cause = it as KafkaProducerException)
                        } ?: run {
                            with(res) {
                                log("opprett oppgave med tekst $tekst, eventid $eventId og ekstern varsling $eksternVarsling", producerRecord.key().fodselsnummer, recordMetadata)
                            }
                            repos.oppgaver.save(Oppgave(fnr.fnr, eventId, ekstern = eksternVarsling)).eventid
                        }
                    }
            }
            else {
                log.trace("Oppretter ikke oppgave i Min Side for $fnr")
                null
            }
        }

    private fun log(msg: String, fnr: String, recordMetadata: RecordMetadata?) =
        with(recordMetadata) {
            log.info("Sendte $msg for ${fnr.partialMask()}, offset ${this?.offset()}, partition ${this?.partition()} på topic ${this?.topic()}")
        }

    @Transactional
    fun avsluttOppgave(fnr: Fødselsnummer, eventId: UUID, type: SkjemaType = STANDARD) =
        with(cfg.oppgave) {
            if (enabled) {
                repos.oppgaver.findByFnrAndEventidAndDoneIsFalse(fnr.fnr, eventId)?.let {
                    avsluttMinSide(it.eventid, fnr, OPPGAVE, type)
                    it.done = true
                } ?: log.warn("Kunne ikke finne oppgave med eventid $eventId for fnr $fnr i DB, allerede avsluttet?. Avslutter på Min Side likevel").also {
                }
            }
            else {
                log.trace("Sender ikke avslutt oppgave til Ditt Nav for $fnr")
            }
        }

    @Transactional
    fun avsluttBeskjed(fnr: Fødselsnummer, eventId: UUID, type: SkjemaType = STANDARD) =
        with(cfg.beskjed) {
            if (enabled) {
                avsluttMinSide(eventId, fnr, BESKJED, type)
                repos.beskjeder.findByFnrAndEventidAndDoneIsFalse(fnr.fnr, eventId)?.let {
                    it.done = true
                }
            }
            else {
                log.trace("Sender ikke avslutt beskjed til Min Side for beskjed for $fnr")
            }
        }
    @Transactional
    fun avsluttAlleTidligereUavsluttedeBeskjederOmMellomlagring(fnr: Fødselsnummer, sisteEventid: UUID, type: SkjemaType = STANDARD) =
        with(cfg.beskjed) {
            if (enabled) {
                repos.beskjeder.findByFnrAndDoneIsFalseAndMellomlagringIsTrueAndEventidNot(fnr.fnr, sisteEventid).forEach {
                    log.trace("Avslutter tidligere, ikke-avsluttet beskjed $it")
                    avsluttMinSide(it.eventid, fnr, BESKJED, type)
                    it.done = true
                }
            }
            else {
                log.trace("Sender ikke avslutt tiligere ikke-avsluttede beskjeder til Min Side for beskjed for $fnr")
            }
        }

    private fun avsluttMinSide(eventId: UUID, fnr: Fødselsnummer, notifikasjonType: NotifikasjonType, type: SkjemaType = STANDARD) =
        minside.send(ProducerRecord(cfg.done, key(type,eventId, fnr), done())).whenComplete { res, e ->
            e?.let {
                throw IntegrationException(msg = "Kunne ikke avslutte ${notifikasjonType.name.lowercase()}", cause = it as KafkaProducerException)
            } ?: run {
                with(res) {
                    when (notifikasjonType) {
                        OPPGAVE -> oppgaverAvsluttet.increment()
                        BESKJED -> beskjederAvsluttet.increment()
                    }
                    log.trace("Sendte avslutt ${notifikasjonType.name.lowercase()} med eventid $eventId  på offset ${recordMetadata.offset()} partition${recordMetadata.partition()}på topic ${recordMetadata.topic()}")
                }
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
        private val oppgaverAvsluttet = Metrics.counter(AVSLUTTET_OPPGAVE)
        private val beskjederAvsluttet = Metrics.counter(AVSLUTTET_BESKJED)
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