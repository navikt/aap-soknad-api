package no.nav.aap.api.søknad.minside

import java.time.Duration
import java.time.LocalDateTime.now
import java.time.ZoneOffset.UTC
import java.util.*
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.SØKNADSTD
import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.DoneInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.OppgaveInputBuilder
import no.nav.tms.utkast.builder.UtkastJsonBuilder

object MinSidePayloadGeneratorer {
     fun beskjed(cfg: MinSideConfig,tekst: String, varighet: Duration, type: MinSideNotifikasjonType, eksternVarsling: Boolean) =
        with(cfg.beskjed) {
            BeskjedInputBuilder()
                .withSikkerhetsnivaa(sikkerhetsnivaa)
                .withTidspunkt(now(UTC))
                .withSynligFremTil(now(UTC).plus(varighet))
                .withLink(type.link(cfg.backlinks)?.toURL())
                .withTekst(tekst)
                .withEksternVarsling(eksternVarsling)
                .withPrefererteKanaler(*preferertekanaler.toTypedArray())
                .build()
        }

     fun oppgave(cfg: MinSideConfig,tekst: String, varighet: Duration, type: MinSideNotifikasjonType, eventId: UUID, eksternVarsling: Boolean) =
        with(cfg.oppgave) {
            OppgaveInputBuilder()
                .withSikkerhetsnivaa(sikkerhetsnivaa)
                .withTidspunkt(now(UTC))
                .withSynligFremTil(now(UTC).plus(varighet))
                .withLink(type.link(cfg.backlinks, eventId)?.toURL())
                .withTekst(tekst)
                .withEksternVarsling(eksternVarsling)
                .withPrefererteKanaler(*preferertekanaler.toTypedArray())
                .build()
        }

    fun done() = DoneInputBuilder().withTidspunkt(now(UTC)).build()

     fun key(cfg: MinSideConfig, eventId: UUID, fnr: Fødselsnummer) =
        with(cfg) {
            NokkelInputBuilder()
                .withFodselsnummer(fnr.fnr)
                .withEventId("$eventId")
                .withAppnavn(app)
                .withNamespace(namespace)
                .build()
        }

     private fun utkast(cfg: MinSideConfig,tittel: String,utkastId: String,fnr: Fødselsnummer) =
        UtkastJsonBuilder()
            .withUtkastId(utkastId)
            .withIdent(fnr.fnr)
            .withLink(SØKNADSTD.link(cfg.backlinks).toString())
            .withTittel(tittel)

     fun lagUtkast(cfg: MinSideConfig,tittel: String,utkastId: String,fnr: Fødselsnummer) = utkast(cfg,tittel,utkastId,fnr).create()
     fun oppdaterUtkast(cfg: MinSideConfig,tittel: String,utkastId: String,fnr: Fødselsnummer,) = utkast(cfg,tittel,utkastId,fnr).update()
     fun slettUtkast(utkastId: String,fnr: Fødselsnummer) =  UtkastJsonBuilder().withUtkastId(utkastId).withIdent(fnr.fnr).delete()


}