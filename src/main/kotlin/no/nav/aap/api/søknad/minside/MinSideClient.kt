package no.nav.aap.api.søknad.minside

import io.micrometer.core.annotation.Counted
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics.counter
import java.time.Duration.between
import java.time.LocalDateTime.now
import java.util.*
import kotlin.time.toKotlinDuration
import no.nav.aap.api.config.Metrikker.AVSLUTTET_BESKJED
import no.nav.aap.api.config.Metrikker.AVSLUTTET_OPPGAVE
import no.nav.aap.api.config.Metrikker.AVSLUTTET_UTKAST
import no.nav.aap.api.config.Metrikker.MELLOMLAGRING
import no.nav.aap.api.config.Metrikker.OPPDATERT_UTKAST
import no.nav.aap.api.config.Metrikker.OPPRETTET_BESKJED
import no.nav.aap.api.config.Metrikker.OPPRETTET_OPPGAVE
import no.nav.aap.api.config.Metrikker.OPPRETTET_UTKAST
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.minside.MinSideBeskjedRepository.Beskjed
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.MINAAPSTD
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.NotifikasjonType
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.NotifikasjonType.BESKJED
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.NotifikasjonType.OPPGAVE
import no.nav.aap.api.søknad.minside.MinSideOppgaveRepository.Oppgave
import no.nav.aap.api.søknad.minside.MinSidePayloadGeneratorer.avsluttUtkast
import no.nav.aap.api.søknad.minside.MinSidePayloadGeneratorer.beskjed
import no.nav.aap.api.søknad.minside.MinSidePayloadGeneratorer.done
import no.nav.aap.api.søknad.minside.MinSidePayloadGeneratorer.key
import no.nav.aap.api.søknad.minside.MinSidePayloadGeneratorer.oppdaterUtkast
import no.nav.aap.api.søknad.minside.MinSidePayloadGeneratorer.oppgave
import no.nav.aap.api.søknad.minside.MinSidePayloadGeneratorer.opprettUtkast
import no.nav.aap.api.søknad.minside.MinSideUtkastRepository.Utkast
import no.nav.aap.api.søknad.minside.UtkastType.CREATED
import no.nav.aap.api.søknad.minside.UtkastType.UPDATED
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.callIdAsUUID
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaOperations
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
data class MinSideProdusenter(val avro: KafkaOperations<NokkelInput, Any>, val utkast: KafkaOperations<String, String>)
@ConditionalOnGCP
class MinSideClient(private val produsenter: MinSideProdusenter,
                    private val cfg: MinSideConfig,
                    private val registry: MeterRegistry,
                    private val repos: MinSideRepositories) {

    private val log = getLogger(javaClass)

    @Transactional
    @Counted(OPPRETTET_UTKAST, description = "Antall utkast opprettet")
    fun opprettUtkast(fnr: Fødselsnummer, tekst: String, skjemaType: SkjemaType = STANDARD, eventId: UUID = callIdAsUUID()) =
        with(cfg.utkast) {
            if (enabled) {
                if (!repos.utkast.existsByFnrAndSkjematype(fnr.fnr, skjemaType)) {
                    if (sendenabled) {
                        log.info("Oppretter Min Side utkast med eventid $eventId")
                        produsenter.utkast.send(ProducerRecord(topic, "$eventId", opprettUtkast(cfg,tekst, "$eventId", fnr)))
                            .get().also {
                                log.trace("Sendte opprett utkast med eventid $eventId  på offset ${it.recordMetadata.offset()} partition${it.recordMetadata.partition()}på topic ${it.recordMetadata.topic()}")
                                repos.utkast.save(Utkast(fnr.fnr, eventId, CREATED))
                                registry.gauge(MELLOMLAGRING, utkast.inc())
                            }
                    } else {
                        log.info("Oppretter Min Side utkast DB med eventid $eventId for $fnr")
                        registry.gauge(MELLOMLAGRING, utkast.inc().also { log.info("Mellomlagring teller er $it") })
                        repos.utkast.save(Utkast(fnr.fnr, eventId, CREATED))
                    }
                }
                else {
                    log.warn("Oppretter IKKE nytt Min Side utkast, fant et allerede eksisterende utkast for $fnr")
                }
            }
            else {
                log.trace("Oppretter IKKE nytt utkast i Ditt Nav for $fnr, disabled")
            }
        }
    @Transactional
    @Counted(OPPDATERT_UTKAST, description = "Antall utkast oppdatert")
    fun oppdaterUtkast(fnr: Fødselsnummer, nyTekst: String, skjemaType: SkjemaType = STANDARD) =
        with(cfg.utkast) {
            if (enabled) {
                repos.utkast.findByFnrAndSkjematype(fnr.fnr, skjemaType)?.let {u ->
                    if (sendenabled) {
                        log.trace("Oppdaterer Min Side utkast med eventid ${u.eventid}")
                        produsenter.utkast.send(ProducerRecord(topic, "${u.eventid}", oppdaterUtkast(cfg,nyTekst, "${u.eventid}", fnr)))
                        .get().also {
                            log.trace("Sendte oppdater utkast med eventid ${u.eventid}  på offset ${it.recordMetadata.offset()} partition${it.recordMetadata.partition()}på topic ${it.recordMetadata.topic()}")
                            repos.utkast.oppdaterUtkast(UPDATED,fnr.fnr, u.eventid)
                        }
                    }
                    else {
                        log.trace("Oppdaterer Min Side utkast DB med eventid ${u.eventid} for $fnr")
                        repos.utkast.oppdaterUtkast(UPDATED,fnr.fnr, u.eventid)
                    }
                } ?:  log.warn("Oppdaterer ikke nytt Min Side utkast, fant IKKE et allerede eksisterende utkast for $fnr")
            }
            else {
                log.trace("Oppdaterer IKKE nytt utkast i Ditt Nav for $fnr, disabled")
            }
        }
    @Counted(AVSLUTTET_UTKAST, description = "Antall utkast slettet")
    @Transactional
    fun avsluttUtkast(fnr: Fødselsnummer,skjemaType: SkjemaType) =
        with(cfg.utkast) {
            if (enabled) {
                repos.utkast.findByFnrAndSkjematype(fnr.fnr,skjemaType)?.let { u ->
                    if (sendenabled) {
                        log.info("Avslutter Min Side utkast for eventid ${u.eventid} etter ${between(u.created, now()).toKotlinDuration()}")
                        produsenter.utkast.send(ProducerRecord(topic,  "${u.eventid}", avsluttUtkast("${u.eventid}",fnr)))
                        .get().also {
                            log.trace("Sendte avslutt utkast med eventid ${u.eventid} på offset ${it.recordMetadata.offset()} partition${it.recordMetadata.partition()}på topic ${it.recordMetadata.topic()}")
                            repos.utkast.delete(u)
                            registry.gauge(MELLOMLAGRING, utkast.decIfPositive())
                        }
                    }
                    else {
                        log.info("Avslutter Min Side utkast DB for eventid ${u.eventid} for $fnr etter ${between(u.created, now()).toKotlinDuration()}")
                        repos.utkast.delete(u)
                        registry.gauge(MELLOMLAGRING, utkast.dec().also { log.info("Mellomlagring teller er $it") })
                    }
                } ?: log.warn("Ingen utkast å avslutte for $fnr")
            }
            else {
                log.trace("Avslutter IKKE utkast i Ditt Nav for $fnr, disabled")
            }
        }
    @Transactional
    fun avsluttUtkastDev(fnr: Fødselsnummer,uuid: UUID) =
        if (cfg.utkast.sendenabled) {
            produsenter.utkast.send(ProducerRecord(cfg.utkast.topic,  "$uuid", avsluttUtkast("$uuid",fnr)))
        }
       else Unit

    @Transactional
    @Counted(OPPRETTET_BESKJED, description = "Antall beskjeder opprettet")
    fun opprettBeskjed(fnr: Fødselsnummer, tekst: String, eventId: UUID = callIdAsUUID(), type: MinSideNotifikasjonType = MINAAPSTD, eksternVarsling: Boolean = true) =
        with(cfg.beskjed) {
            if (enabled) {
                log.trace("Oppretter Min Side beskjed med ekstern varsling $eksternVarsling og eventid $eventId")
                produsenter.avro.send(ProducerRecord(topic, key(cfg, eventId, fnr), beskjed(cfg,tekst, varighet,type, eksternVarsling)))
                    .get().run {
                        log.trace("Sendte opprett beskjed med eventid $eventId og ekstern varsling $eksternVarsling på offset ${recordMetadata.offset()} partition${recordMetadata.partition()}på topic ${recordMetadata.topic()}")
                        repos.beskjeder.save(Beskjed(fnr.fnr, eventId, ekstern = eksternVarsling)).eventid
                    }
            }
            else {
                log.trace("Oppretter IKKE beskjed i Ditt Nav for $fnr, disabled")
            }
        }

    @Transactional
    fun avsluttBeskjed(fnr: Fødselsnummer, eventId: UUID) =
        with(cfg.beskjed) {
            if (enabled) {
                avslutt(eventId, fnr, BESKJED)
                repos.beskjeder.findByFnrAndEventidAndDoneIsFalse(fnr.fnr, eventId)?.let {
                    it.done = true
                }
            }
            else {
                log.trace("Sender IKKE avslutt beskjed til Min Side for beskjed for $fnr, disabled")
            }
        }

    @Transactional
    @Counted(OPPRETTET_OPPGAVE, description = "Antall oppgaver opprettet")
    fun opprettOppgave(fnr: Fødselsnummer, tekst: String, eventId: UUID = callIdAsUUID(), type: MinSideNotifikasjonType = MINAAPSTD, eksternVarsling: Boolean = true) =
        with(cfg.oppgave) {
            if (enabled) {
                log.trace("Oppretter Min Side oppgave med ekstern varsling $eksternVarsling og eventid $eventId")
                produsenter.avro.send(ProducerRecord(topic, key(cfg, eventId, fnr),
                        oppgave(cfg,tekst, varighet, type, eventId, eksternVarsling)))
                    .get().run {
                        log.trace("Sendte opprett oppgave med eventid $eventId og ekstern varsling $eksternVarsling på offset ${recordMetadata.offset()} partition${recordMetadata.partition()}på topic ${recordMetadata.topic()}")
                        repos.oppgaver.save(Oppgave(fnr.fnr, eventId, ekstern = eksternVarsling)).eventid
                    }
            }
            else {
                log.trace("Oppretter IKKE oppgave i Min Side for $fnr")
            }
        }

    @Transactional
    fun avsluttOppgave(fnr: Fødselsnummer, eventId: UUID) =
        with(cfg.oppgave) {
            if (enabled) {
                avslutt(eventId, fnr, OPPGAVE)
                repos.oppgaver.findByFnrAndEventidAndDoneIsFalse(fnr.fnr, eventId)?.let {
                    it.done = true
                }
            }
            else {
                log.trace("Sender IKKE avslutt oppgave til Ditt Nav for $fnr, disabled")
            }
        }

    private fun avslutt(eventId: UUID, fnr: Fødselsnummer, notifikasjonType: NotifikasjonType) =
        produsenter.avro.send(ProducerRecord(cfg.done, key(cfg, eventId, fnr), done())).get().run {
            when (notifikasjonType) {
                OPPGAVE -> oppgaverAvsluttet.increment()
                BESKJED -> beskjederAvsluttet.increment()
            }.also {
                log.trace("Sendte avslutt $notifikasjonType med eventid $eventId  på offset ${recordMetadata.offset()} partition${recordMetadata.partition()}på topic ${recordMetadata.topic()}")
            }
        }

    companion object {
        private fun Int.decIfPositive() = if (this > 0) dec() else this
        private var utkast = 0
        private val oppgaverAvsluttet = counter(AVSLUTTET_OPPGAVE)
        private val beskjederAvsluttet = counter(AVSLUTTET_BESKJED)
    }
}

enum class UtkastType  {CREATED, UPDATED,DONE }