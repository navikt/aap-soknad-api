package no.nav.aap.api.søknad.minside

import io.micrometer.core.annotation.Counted
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics.counter
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
import no.nav.aap.api.søknad.SendCallback
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
import no.nav.aap.api.søknad.minside.MinSidePayloadGeneratorer.oppgave
import no.nav.aap.api.søknad.minside.MinSidePayloadGeneratorer.opprettUtkast
import no.nav.aap.api.søknad.minside.MinSideUtkastRepository.Utkast
import no.nav.aap.api.søknad.minside.UtkastType.CREATED
import no.nav.aap.api.søknad.minside.UtkastType.DONE
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
class MinSideClient(private val minside: MinSideProdusenter,
                    private val cfg: MinSideConfig,
                    private val registry: MeterRegistry,
                    private val repos: MinSideRepositories) {

    private val log = getLogger(javaClass)

    @Counted(value = OPPRETTET_UTKAST, description = "Antall utkast opprettet")
    @Transactional
    fun opprettUtkast(fnr: Fødselsnummer, tekst: String, skjemaType: SkjemaType = STANDARD, eventId: UUID = callIdAsUUID()) =
        with(cfg.utkast) {
            if (enabled) {
                if (repos.utkast.existsByFnrAndSkjematype(fnr.fnr, skjemaType)) {
                    registry.gauge(MELLOMLAGRING, utkast.inc())
                    log.info("Oppretter Min Side utkast med eventid $eventId")
                    minside.utkast.send(ProducerRecord(topic, "$eventId", opprettUtkast(cfg,tekst, "$eventId", fnr)))
                        .addCallback(SendCallback("opprett utkast med tekst $tekst,  eventid $eventId") { repos.utkast.save(Utkast(fnr.fnr, eventId, CREATED)) })
                        //.get().run {
                         //   log.trace("Sendte opprett utkast med tekst $tekst, eventid $eventId  på offset ${recordMetadata.offset()} partition${recordMetadata.partition()}på topic ${recordMetadata.topic()}")
                        //    repos.utkast.save(Utkast(fnr.fnr, eventId, CREATED))
                        //}
                }
                else {
                    log.trace("Oppretter ikke nytt Min Side utkast, fant et allerede eksisterende utkast")
                }
            }
            else {
                log.trace("Oppretter ikke nytt utkast i Ditt Nav for $fnr")
                null
            }
        }
    fun foo(bar: () -> String) {
        print(bar.invoke())
    }
    @Counted(value = AVSLUTTET_UTKAST, description = "Antall utkast slettet")
    @Transactional
    fun avsluttUtkast(fnr: Fødselsnummer,skjemaType: SkjemaType) =
        with(cfg.utkast) {
            if (enabled) {
                repos.utkast.findByFnrAndSkjematype(fnr.fnr,skjemaType)?.let {
                    registry.gauge(MELLOMLAGRING, utkast.decIfPositive())
                    log.info("Avslutter Min Side utkast for eventid $it")
                    minside.utkast.send(ProducerRecord(topic,  "${it.eventid}", avsluttUtkast("${it.eventid}",fnr)))
                        .get().run {
                            log.trace("Sendte avslutt utkast eventid ${it.eventid} på offset ${recordMetadata.offset()} partition${recordMetadata.partition()}på topic ${recordMetadata.topic()}")
                            it.done = true
                            it.type = DONE
                        }
                } ?: log.trace("Ingen utkast å avslutte for $fnr")
            }
            else {
                log.trace("Oppretter ikke utkast i Ditt Nav for $fnr, disabled")
                null
            }
        }


    @Transactional
    @Counted(value = OPPRETTET_BESKJED, description = "Antall beskjeder opprettet")
    fun opprettBeskjed(fnr: Fødselsnummer, tekst: String, eventId: UUID = callIdAsUUID(), type: MinSideNotifikasjonType = MINAAPSTD, eksternVarsling: Boolean = true) =
        with(cfg.beskjed) {
            if (enabled) {
                log.info("Oppretter Min Side beskjed med ekstern varsling $eksternVarsling og eventid $eventId")
                minside.avro.send(ProducerRecord(topic, key(cfg, eventId, fnr), beskjed(cfg,tekst, varighet,type, eksternVarsling)))
                    .get().run {
                        log.trace("Sendte opprett beskjed med tekst $tekst, eventid $eventId og ekstern varsling $eksternVarsling på offset ${recordMetadata.offset()} partition${recordMetadata.partition()}på topic ${recordMetadata.topic()}")
                        repos.beskjeder.save(Beskjed(fnr.fnr, eventId, ekstern = eksternVarsling)).eventid
                    }
            }
            else {
                log.trace("Oppretter ikke beskjed i Ditt Nav for $fnr")
                null
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
                log.trace("Sender ikke avslutt beskjed til Min Side for beskjed for $fnr")
            }
        }

    @Transactional
    @Counted(value = OPPRETTET_OPPGAVE, description = "Antall oppgaver opprettet")
    fun opprettOppgave(fnr: Fødselsnummer, tekst: String, eventId: UUID = callIdAsUUID(), type: MinSideNotifikasjonType = MINAAPSTD, eksternVarsling: Boolean = true) =
        with(cfg.oppgave) {
            if (enabled) {
                log.info("Oppretter Min Side oppgave med ekstern varsling $eksternVarsling og eventid $eventId")
                minside.avro.send(ProducerRecord(topic, key(cfg, eventId, fnr),
                        oppgave(cfg,tekst, varighet, type, eventId, eksternVarsling)))
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
    fun avsluttOppgave(fnr: Fødselsnummer, eventId: UUID) =
        with(cfg.oppgave) {
            if (enabled) {
                avslutt(eventId, fnr, OPPGAVE)
                repos.oppgaver.findByFnrAndEventidAndDoneIsFalse(fnr.fnr, eventId)?.let {
                    it.done = true
                }
            }
            else {
                log.trace("Sender ikke avslutt oppgave til Ditt Nav for $fnr")
            }
        }

    private fun avslutt(eventId: UUID, fnr: Fødselsnummer, notifikasjonType: NotifikasjonType) =
        minside.avro.send(ProducerRecord(cfg.done, key(cfg, eventId, fnr), done())).get().run {
            when (notifikasjonType) {
                OPPGAVE -> oppgaverAvsluttet.increment()
                BESKJED -> beskjederAvsluttet.increment()
            }.also {
                log.info("Sendte avslutt $notifikasjonType med eventid $eventId  på offset ${recordMetadata.offset()} partition${recordMetadata.partition()}på topic ${recordMetadata.topic()}")
            }
        }

    companion object {
        private fun Int.decIfPositive() = if (this > 0) this.dec() else this
        private var utkast = 0
        private val oppgaverAvsluttet = counter(AVSLUTTET_OPPGAVE)
        private val beskjederAvsluttet = counter(AVSLUTTET_BESKJED)
    }
}

enum class UtkastType  {CREATED, UPDATED,DONE }