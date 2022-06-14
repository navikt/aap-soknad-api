package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavCallbacks.DittNavBeskjedCallback
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavCallbacks.DittNavDoneCallback
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavCallbacks.DittNavOppgaveCallback
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

@ConditionalOnGCP
class DittNavClient(private val dittNav: KafkaOperations<NokkelInput, Any>,
                    private val cfg: DittNavConfig,
                    private val repos: DittNavRepositories,
                    private val ctx: AuthContext) {
    init {
        log.info("CONFIG er $cfg")
    }

    fun opprettBeskjed() = opprettBeskjed(varighet = cfg.beskjed.varighet)
    fun opprettBeskjed(type: SkjemaType = STANDARD,
                       tekst: String = "Mottatt ${type.tittel}",
                       varighet: Duration) =
        if (cfg.beskjed.enabled) {
            with(nøkkelInput(type.name, callId(), "beskjed")) {
                dittNav.send(ProducerRecord(cfg.beskjed.topic, this, beskjed(type, tekst, varighet)))
                    .addCallback(DittNavBeskjedCallback(this, repos.beskjed))
                eventId
            }
        }
        else {
            log.info("Sender ikke beskjed til Ditt Nav")
            null
        }

    fun opprettOppgave(type: SkjemaType, tekst: String, varighet: Duration = cfg.oppgave.varighet) =
        if (cfg.oppgave.enabled) {
            with(nøkkelInput(type.name, callId(), "oppgave")) {
                dittNav.send(ProducerRecord(cfg.oppgave.topic, this, oppgave(type, tekst, varighet)))
                    .addCallback(DittNavOppgaveCallback(this, repos.oppgave))
                eventId
            }
        }
        else {
            log.info("Sender ikke oppgave til Ditt Nav")
            null
        }

    fun avslutt(type: SkjemaType, eventId: String) =
        if (cfg.done.enabled) {
            with(nøkkelInput(type.name, eventId, "done")) {
                dittNav.send(ProducerRecord(cfg.done.topic, this, avslutt()))
                    .addCallback(DittNavDoneCallback(this, repos.oppgave))
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

    private fun nøkkelInput(grupperingId: String, eventId: String, type: String) =
        with(cfg) {
            NokkelInputBuilder()
                .withFodselsnummer(ctx.getSubject())
                .withEventId(eventId)
                .withGrupperingsId(grupperingId)
                .withAppnavn(app)
                .withNamespace(namespace)
                .build()
                .also {
                    log.info(CONFIDENTIAL, "Key for Ditt Nav $type er $this")
                }
        }

    @Transactional
    fun opprettMellomlagringBeskjed(uuid: String?, varighet: Duration) {
        uuid?.let { u ->
            val s = JPASøknad(fnr = ctx.getFnr().fnr, ref = u, gyldigtil = now().plus(varighet)).also {
                log.info("Mellomlagrer $it")
            }
            repos.søknader.saveAndFlush(s).also {
                log.info("Mellomlagret $it")
            }
        } ?: log.info("Ingen mellomlagring")
    }

    @Transactional
    fun fjernGamleMellomlagringer() =
        repos.søknader.deleteByGyldigtilBefore(now()).also { log.info("Fjernet $it gamle rader") }

    @Transactional
    fun fjernMellomlagringer() {
        val deleted = repos.søknader.deleteByFnr(ctx.getFnr().fnr).also { log.info("Fjernet mellomlagring rad") }
        log.info("Fjernet  mellomlagring $deleted")
        deleted.firstOrNull()?.let { avslutt(STANDARD, it.ref!!) }
    }

    @Transactional(readOnly = true)
    fun harOpprettetMellomlagringBeskjed() =
        repos.søknader.getByFnr(ctx.getFnr().fnr) != null

    companion object {
        private val log = getLogger(DittNavClient::class.java)
    }
}