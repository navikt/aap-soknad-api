package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavCallbacks.DittNavBeskjedCallback
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavCallbacks.DittNavBeskjedDoneCallback
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavCallbacks.DittNavOppgaveCallback
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavCallbacks.DittNavOppgaveDoneCallback
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
                       fnr: Fødselsnummer,
                       eventId: UUID,
                       tekst: String,
                       mellomlager: Boolean = false) =
        with(cfg.beskjed) {
            if (enabled) {
                with(nøkkel(type.name, "$eventId", fnr, "beskjed")) {
                    dittNav.send(ProducerRecord(topic, this, beskjed(type, tekst)))
                        .addCallback(DittNavBeskjedCallback(this))
                    repos.beskjeder.save(JPADittNavBeskjed(fnr = fnr.fnr,
                            eventid = "$eventId",
                            mellomlager = mellomlager))
                    eventId
                }

            }
            else {
                log.info("Sender ikke beskjed til Ditt Nav")
                callId()
            }
        }

    @Transactional
    fun opprettOppgave(type: SkjemaType, fnr: Fødselsnummer, eventId: UUID, tekst: String) =
        with(cfg.oppgave) {
            if (enabled) {
                with(nøkkel(type.name, "$eventId", fnr, "oppgave")) {
                    dittNav.send(ProducerRecord(topic, this, oppgave(type, tekst)))
                        .addCallback(DittNavOppgaveCallback(this))
                    repos.oppgaver.save(JPADittNavOppgave(fnr = fnr.fnr, eventid = "$eventId"))
                    eventId
                }
            }
            else {
                log.info("Sender ikke oppgave til Ditt Nav")
                callId()
            }
        }

    @Transactional
    fun avsluttOppgave(type: SkjemaType, fnr: Fødselsnummer, eventId: UUID) =
        with(cfg) {
            if (oppgave.enabled) {
                with(nøkkel(type.name, "$eventId", fnr, "done")) {
                    dittNav.send(ProducerRecord(done.topic, this, done()))
                        .addCallback(DittNavOppgaveDoneCallback(this, repos.oppgaver))
                }
            }
            else {
                log.info("Sender ikke done til Ditt Nav")
            }
        }

    fun avsluttBeskjed(type: SkjemaType, fnr: Fødselsnummer, eventId: UUID) =
        with(cfg) {
            if (beskjed.enabled) {
                with(nøkkel(type.name, "$eventId", fnr, "done")) {
                    dittNav.send(ProducerRecord(done.topic, this, done()))
                        .addCallback(DittNavBeskjedDoneCallback(this, repos.beskjeder))
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

    private fun nøkkel(grupperingId: String, eventId: String, fnr: Fødselsnummer, type: String) =
        with(cfg) {
            NokkelInputBuilder()
                .withFodselsnummer(fnr.fnr)
                .withEventId(eventId)
                .withGrupperingsId(grupperingId)
                .withAppnavn(app)
                .withNamespace(namespace)
                .build().also { log.info(CONFIDENTIAL, "Key for Ditt Nav $type er $it") }
        }
}