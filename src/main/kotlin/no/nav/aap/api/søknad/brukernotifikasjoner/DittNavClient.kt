package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.AuthContextExtension.getJti
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri
import java.time.Duration
import java.time.LocalDateTime.now
import java.time.ZoneOffset.UTC

@ConditionalOnGCP
class DittNavClient(private val dittNav: KafkaOperations<NokkelInput, Any>,
                    private val cfg: DittNavConfig,
                    private val repos: DittNavRepositories,
                    private val ctx: AuthContext) {

    private val log = getLogger(javaClass)

    fun opprettBeskjed(type: SkjemaType = STANDARD,
                       tekst: String = "Mottatt ${type.tittel}",
                       varighet: Duration = cfg.beskjed.varighet) =
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

    fun avsluttOppgave(type: SkjemaType, eventId: String) =
        if (cfg.done.enabled) {
            with(nøkkelInput(type.name, eventId, "done")) {
                dittNav.send(ProducerRecord(cfg.done.topic, this, avsluttOppgave()))
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

    private fun avsluttOppgave() =
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

    fun opprettMellomlagringBeskjed(uuid: String?) {
        uuid?.let {
            val s = JPASøknad(fnr = ctx.getFnr().fnr, ref = it, jti = ctx.getJti())
            repos.søknader.saveAndFlush(s)
        } ?: log.info("Ingen mellomlagring")
    }

    fun opprettetMellomlagringBeskjed() = repos.søknader.getByJtiAndFnr(ctx.getJti(), ctx.getFnr().fnr)

}