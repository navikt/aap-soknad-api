package no.nav.aap.api.søknad.brukernotifikasjoner

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
import java.time.Duration
import java.time.LocalDateTime.now
import java.time.ZoneOffset.UTC

@ConditionalOnGCP
class DittNavClient(private val dittNav: KafkaOperations<NokkelInput, Any>,
                    private val cfg: DittNavConfig,
                    private val repos: DittNavRepositories) {

    private val log = getLogger(javaClass)

    @Transactional
    fun opprettBeskjed(type: SkjemaType = STANDARD,
                       eventId: String = callId(),
                       fnr: String,
                       tekst: String) =
        with(cfg.beskjed) {
            if (enabled) {
                with(nøkkel(type.name, eventId, fnr, "beskjed")) {
                    dittNav.send(ProducerRecord(topic, this, beskjed(type, tekst)))
                        .addCallback(DittNavBeskjedCallback(this))
                    repos.beskjeder.save(JPADittNavBeskjed(fnr = fnr, eventid = eventId))
                    eventId
                }

            }
            else {
                log.info("Sender ikke beskjed til Ditt Nav")
                callId()
            }
        }

    @Transactional
    fun opprettOppgave(type: SkjemaType, fnr: String, tekst: String) =
        with(cfg.oppgave) {
            if (enabled) {
                with(nøkkel(type.name, callId(), fnr, "oppgave")) {
                    dittNav.send(ProducerRecord(topic, this, oppgave(type, tekst)))
                        .addCallback(DittNavOppgaveCallback(this))
                    repos.oppgaver.save(JPADittNavOppgave(fnr = fnr, eventid = eventId))
                    eventId
                }
            }
            else {
                log.info("Sender ikke oppgave til Ditt Nav")
                callId()
            }
        }

    @Transactional
    fun avsluttOppgave(type: SkjemaType = STANDARD, fnr: String, eventId: String) =
        with(cfg) {
            if (oppgave.enabled) {
                with(nøkkel(type.name, eventId, fnr, "done")) {
                    dittNav.send(ProducerRecord(done.topic, this, done()))
                        .addCallback(DittNavOppgaveDoneCallback(this))
                    repos.oppgaver.done(eventId)

                }
            }
            else {
                log.info("Sender ikke done til Ditt Nav")
            }
        }

    @Transactional
    fun avsluttBeskjed(type: SkjemaType = STANDARD, fnr: String, eventId: String) =
        with(cfg) {
            if (beskjed.enabled) {
                with(nøkkel(type.name, eventId, fnr, "done")) {
                    dittNav.send(ProducerRecord(done.topic, this, done()))
                        .addCallback(DittNavBeskjedDoneCallback(this))
                    repos.beskjeder.done(eventId)
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

    private fun nøkkel(grupperingId: String, eventId: String, fnr: String, type: String) =
        with(cfg) {
            NokkelInputBuilder()
                .withFodselsnummer(fnr)
                .withEventId(eventId)
                .withGrupperingsId(grupperingId)
                .withAppnavn(app)
                .withNamespace(namespace)
                .build().also { log.info(CONFIDENTIAL, "Key for Ditt Nav $type er $it") }
        }

    private fun opprettMellomlagringBeskjed(eventId: String) =
        repos.søknader.saveAndFlush(JPASøknad(eventid = eventId,
                gyldigtil = now().plus(Duration.ofDays(cfg.mellomlagring)))).also {
            log.trace(CONFIDENTIAL, "Opprettet mellomlagring rad OK $it")
        }

    private fun fjernAlleGamleMellomlagringer() = repos.søknader.deleteByGyldigtilBefore(now())

    @Transactional
    fun fjernOgAvsluttMellomlagring(fnr: String) {
        repos.søknader.deleteByFnr(fnr)/*.also { rows ->
            rows?.firstOrNull()?.let { jpa ->
                avsluttBeskjed(eventId = jpa.eventid, fnr = fnr).also {
                    log.trace(CONFIDENTIAL, "Fjernet mellomlagring rad ${jpa.eventid}")
                }
            }*/
    }
}

//@Transactional
fun init() = Unit
/*
log.info("Fjerner gamle mellomlagringer")
fjernAlleGamleMellomlagringer().also {
    log.info("Fjernet $it gamle mellomlagringer OK")
}
 fjernOgAvsluttMellomlagring() // TODO
log.trace("Oppdaterer mellomlagring beskjed og DB")
opprettBeskjed(tekst = "Du har en påbegynt søknad om AAP").also { uuid ->
    opprettMellomlagringBeskjed(uuid).also {
        log.trace("Eventid for opprettet beskjed om mellomlagring er $uuid")
    }
}*/


//@Transactional
fun finalize() = Unit  // TODO
//  opprettBeskjed(STANDARD, tekst = "Vi har mottatt en søknad om AAP").also {
// fjernOgAvsluttMellomlagring()  // TODO
// }