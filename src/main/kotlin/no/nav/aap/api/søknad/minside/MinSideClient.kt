package no.nav.aap.api.søknad.minside

import io.micrometer.core.annotation.Counted
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics.counter
import java.time.Duration
import java.time.LocalDateTime.now
import java.time.ZoneOffset.UTC
import java.util.*
import no.nav.aap.api.config.Metrikker.AVSLUTTET_BESKJED
import no.nav.aap.api.config.Metrikker.AVSLUTTET_OPPGAVE
import no.nav.aap.api.config.Metrikker.AVSLUTTET_UTKAST
import no.nav.aap.api.config.Metrikker.MELLOMLAGRING
import no.nav.aap.api.config.Metrikker.OPPRETTET_BESKJED
import no.nav.aap.api.config.Metrikker.OPPRETTET_OPPGAVE
import no.nav.aap.api.config.Metrikker.OPPRETTET_UTKAST
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.UTLAND_SØKNAD
import no.nav.aap.api.søknad.minside.MinSideBeskjedRepository.Beskjed
import no.nav.aap.api.søknad.minside.MinSideClient.NotifikasjonType.BESKJED
import no.nav.aap.api.søknad.minside.MinSideClient.NotifikasjonType.OPPGAVE
import no.nav.aap.api.søknad.minside.MinSideConfig.BacklinksConfig
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.MINAAPSTD
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.SØKNADSTD
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.MinSideBacklinkContext.MINAAP
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.MinSideBacklinkContext.SØKNAD
import no.nav.aap.api.søknad.minside.MinSideOppgaveRepository.Oppgave
import no.nav.aap.api.søknad.minside.MinSideUtkastRepository.Utkast
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.callIdAsUUID
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.DoneInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.OppgaveInputBuilder
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.tms.utkast.builder.UtkastJsonBuilder
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaOperations
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder.fromUri

@ConditionalOnGCP
class MinSideClient(private val minside: KafkaOperations<NokkelInput, Any>,
                    private val utkast: KafkaOperations<String, String>,
                    private val cfg: MinSideConfig,
                    private val registry: MeterRegistry,
                    private val repos: MinSideRepositories) {

    private val log = getLogger(javaClass)



    @Counted(value = AVSLUTTET_UTKAST, description = "Antall utkast slettet")
    @Transactional
    fun avsluttUtkast(fnr: Fødselsnummer) =
        with(cfg.utkast) {
            if (enabled) {
                repos.utkast.findByFnr(fnr.fnr)?.let {
                    registry.gauge(MELLOMLAGRING, mellomlagrede.decIfPositive())
                    log.info("Avslutter Min Side utkast for eventid $it")
                    utkast.send(ProducerRecord(topic,  "${it.eventid}", slettUtkast("${it.eventid}",fnr)))
                        .get().run {
                            log.trace("Sendte avslutt utkast eventid ${it.eventid} på offset ${recordMetadata.offset()} partition${recordMetadata.partition()}på topic ${recordMetadata.topic()}")
                            it.done = true
                            it.type = "deleted"
                        }
                } ?: log.trace("Ingen utkast å avslutte for $fnr")
            }
            else {
                log.trace("Oppretter ikke utkast i Ditt Nav for $fnr, disabled")
                null
            }
        }

    @Counted(value = OPPRETTET_UTKAST, description = "Antall utkast opprettet")
    @Transactional
    fun opprettUtkast(fnr: Fødselsnummer,
                      tekst: String,
                      eventId: UUID = callIdAsUUID()) =
        with(cfg.utkast) {
            if (enabled) {
                val u = repos.utkast.findByFnr(fnr.fnr)
                if (u == null) {
                    registry.gauge(MELLOMLAGRING, mellomlagrede.inc())
                    log.info("Oppretter Min Side utkast med eventid $eventId")
                    utkast.send(ProducerRecord(topic,  "$eventId", lagUtkast(tekst, "$eventId",fnr)))
                        .get().run {
                            log.trace("Sendte opprett utkast med tekst $tekst, eventid $eventId  på offset ${recordMetadata.offset()} partition${recordMetadata.partition()}på topic ${recordMetadata.topic()}")
                            repos.utkast.save(Utkast(fnr.fnr,eventId,"created"))
                        }
                }
                else {
                    log.trace("Oppretter ikke nytt Min Side utkast, fant allerede eksisterende innslag")
                }
            }
            else {
                log.trace("Oppretter ikke nytt utkast i Ditt Nav for $fnr")
                null
            }
        }

    private fun utkast(tittel: String,utkastId: String,fnr: Fødselsnummer) =
         UtkastJsonBuilder()
             .withUtkastId(utkastId)
             .withIdent(fnr.fnr)
             .withLink(SØKNADSTD.link(cfg.backlinks).toString())
             .withTittel(tittel)

    private fun lagUtkast(tittel: String,utkastId: String,fnr: Fødselsnummer,) = utkast(tittel,utkastId,fnr).create()
    private fun oppdaterUtkast(tittel: String,utkastId: String,fnr: Fødselsnummer,) = utkast(tittel,utkastId,fnr).update()
    private fun slettUtkast(utkastId: String,fnr: Fødselsnummer) =  UtkastJsonBuilder().withUtkastId(utkastId).withIdent(fnr.fnr).delete()

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
                log.info("Oppretter Min Side beskjed med ekstern varsling $eksternVarsling og eventid $eventId")
                minside.send(ProducerRecord(topic, key(type.skjemaType, eventId, fnr), beskjed(tekst, varighet,type, eksternVarsling)))
                    .get().run {
                        log.trace("Sendte opprett beskjed med tekst $tekst, eventid $eventId og ekstern varsling $eksternVarsling på offset ${recordMetadata.offset()} partition${recordMetadata.partition()}på topic ${recordMetadata.topic()}")
                        repos.beskjeder.save(Beskjed(fnr.fnr, eventId, mellomlagring = mellomlagring,ekstern = eksternVarsling)).eventid
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
                    .get().run {
                        log.trace("Sendte opprett oppgave med tekst $tekst, eventid $eventId og ekstern varsling $eksternVarsling på offset ${recordMetadata.offset()} partition${recordMetadata.partition()}på topic ${recordMetadata.topic()}")
                        repos.oppgaver.save(Oppgave(fnr.fnr, eventId, ekstern = eksternVarsling)).eventid
                    }
            }
            else {
                log.trace("Oppretter ikke oppgave i Min Side for $fnr")
                null
            }
        }

    @Transactional
    fun avsluttOppgave(fnr: Fødselsnummer, eventId: UUID, type: SkjemaType = STANDARD) =
        with(cfg.oppgave) {
            if (enabled) {
                avsluttMinSide(eventId, fnr, OPPGAVE, type)
                repos.oppgaver.findByFnrAndEventidAndDoneIsFalse(fnr.fnr, eventId)?.let {
                    it.done = true
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

    private fun avsluttMinSide(eventId: UUID, fnr: Fødselsnummer, notifikasjonType: NotifikasjonType, type: SkjemaType = STANDARD) =
        minside.send(ProducerRecord(cfg.done, key(type,eventId, fnr), done())).get().run {
            when (notifikasjonType) {
                OPPGAVE -> oppgaverAvsluttet.increment()
                BESKJED -> beskjederAvsluttet.increment()
            }.also {
                log.info("Sendte avslutt $notifikasjonType med eventid $eventId  på offset ${recordMetadata.offset()} partition${recordMetadata.partition()}på topic ${recordMetadata.topic()}")
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
        private fun Int.decIfPositive() = if (this > 0) this.dec() else this
        private var mellomlagrede = 0
        private val oppgaverAvsluttet = counter(AVSLUTTET_OPPGAVE)
        private val beskjederAvsluttet = counter(AVSLUTTET_BESKJED)
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