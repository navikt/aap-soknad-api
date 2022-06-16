package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavCallbacks.DittNavBeskjedCallback
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavCallbacks.DittNavBeskjedDoneCallback
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavCallbacks.DittNavOppgaveCallback
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavCallbacks.DittNavOppgaveDoneCallback
import no.nav.aap.util.AuthContext
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
import java.util.*

@ConditionalOnGCP
class DittNavClient(private val dittNav: KafkaOperations<NokkelInput, Any>,
                    private val cfg: DittNavConfig,
                    private val repos: DittNavRepositories,
                    private val ctx: AuthContext) {

    fun opprettBeskjed(type: SkjemaType = STANDARD, tekst: String = "Vi har mottatt en ${type.tittel}") =
        opprettBeskjed(type, tekst, cfg.beskjed.varighet)

    internal fun opprettBeskjed(type: SkjemaType, tekst: String, varighet: Duration) =
        if (cfg.beskjed.enabled) {
            with(nøkkel(type.name, callId(), "beskjed")) {
                dittNav.send(ProducerRecord(cfg.beskjed.topic, this, beskjed(type, tekst, varighet)))
                    .addCallback(DittNavBeskjedCallback(this))
                repos.beskjed.save(JPADittNavBeskjed(fnr = ctx.getFnr().fnr, ref = eventId))
                eventId
            }

        }
        else {
            log.info("Sender ikke beskjed til Ditt Nav")
            UUID.randomUUID().toString()
        }

    fun opprettOppgave(type: SkjemaType, tekst: String, varighet: Duration = cfg.oppgave.varighet) =
        if (cfg.oppgave.enabled) {
            with(nøkkel(type.name, callId(), "oppgave")) {
                dittNav.send(ProducerRecord(cfg.oppgave.topic, this, oppgave(type, tekst, varighet)))
                    .addCallback(DittNavOppgaveCallback(this))
                eventId
            }
        }
        else {
            log.info("Sender ikke oppgave til Ditt Nav")
            null
        }

    fun avsluttOppgave(type: SkjemaType = STANDARD, eventId: String) =
        if (cfg.done.enabled) {
            with(nøkkel(type.name, eventId, "done")) {
                dittNav.send(ProducerRecord(cfg.done.topic, this, avslutt()))
                    .addCallback(DittNavOppgaveDoneCallback(this))
                repos.oppgave.done(eventId)

            }
        }
        else {
            log.info("Sender ikke done til Ditt Nav")
        }

    fun avsluttBeskjed(type: SkjemaType = STANDARD, eventId: String) =
        if (cfg.done.enabled) {
            with(nøkkel(type.name, eventId, "done")) {
                dittNav.send(ProducerRecord(cfg.done.topic, this, avslutt()))
                    .addCallback(DittNavBeskjedDoneCallback(this))
                repos.beskjed.done(eventId)
            }
        }
        else {
            log.info("Sender ikke done til Ditt Nav")
        }

    private fun beskjed(type: SkjemaType, tekst: String, varighet: Duration) =
        with(cfg.beskjed) {
            BeskjedInputBuilder()
                .withSikkerhetsnivaa(sikkerhetsnivaa)
                .withTidspunkt(now(UTC))
                .withSynligFremTil(now(UTC).plus(varighet))
                .withLink(replaceWith("/aap/${type.name}"))
                .withTekst(tekst)
                .withEksternVarsling(eksternVarsling)
                .withPrefererteKanaler(*preferertekanaler.toTypedArray())
                .build()
        }

    private fun oppgave(type: SkjemaType, tekst: String, varighet: Duration) =
        with(cfg.oppgave) {
            OppgaveInputBuilder()
                .withSikkerhetsnivaa(sikkerhetsnivaa)
                .withTidspunkt(now(UTC))
                .withSynligFremTil(now(UTC).plus(varighet))
                .withLink(replaceWith("/aap/${type.name}"))
                .withTekst(tekst)
                .withEksternVarsling(eksternVarsling)
                .withPrefererteKanaler(*preferertekanaler.toTypedArray())
                .build()
        }

    private fun avslutt() =
        DoneInputBuilder()
            .withTidspunkt(now(UTC))
            .build()

    private fun replaceWith(replacement: String) =
        fromCurrentRequestUri().replacePath(replacement).build().toUri().toURL()

    private fun nøkkel(grupperingId: String, eventId: String, type: String) =
        with(cfg) {
            NokkelInputBuilder()
                .withFodselsnummer(ctx.getSubject())
                .withEventId(eventId)
                .withGrupperingsId(grupperingId)
                .withAppnavn(app)
                .withNamespace(namespace)
                .build().also { log.info(CONFIDENTIAL, "Key for Ditt Nav $type er $it") }
        }

    // @Transactional
    internal fun opprettMellomlagringBeskjed(id: String) =
        repos.søknader.saveAndFlush(JPASøknad(fnr = ctx.getFnr().fnr,
                ref = id,
                gyldigtil = now().plus(Duration.ofDays(cfg.mellomlagring)))).also {
            log.trace(CONFIDENTIAL, "Opprettet mellomlagring rad OK $it")
        }

    internal fun fjernAlleGamleMellomlagringer() = repos.søknader.deleteByGyldigtilBefore(now())

    //@Transactional
    internal fun fjernOgAvsluttMellomlagring() {
        repos.søknader.deleteByFnr(ctx.getFnr().fnr).also { rows ->
            rows?.firstOrNull()?.let {
                log.trace(CONFIDENTIAL, "Fjernet mellomlagring rad $it")
                avsluttBeskjed(eventId = it.ref!!)
            }
        }
    }

    @Transactional
    fun init() {
        log.info("Fjerner gamle mellomlagringer")
        fjernAlleGamleMellomlagringer().also {
            log.info("Fjernet $it gamle mellomlagringer OK")
        }
        fjernOgAvsluttMellomlagring()
        log.trace("Oppdaterer mellomlagring beskjed og DB")
        opprettBeskjed(tekst = "Du har en påbegynt søknad om AAP").also { uuid ->
            log.trace("uuid for opprettet beskjed om mellomlagring er $uuid")
            opprettMellomlagringBeskjed(uuid)
        }
    }

    @Transactional
    fun exit() =
        opprettBeskjed().also {
            fjernOgAvsluttMellomlagring()
        }

    companion object {
        private val log = getLogger(DittNavClient::class.java)
    }
}