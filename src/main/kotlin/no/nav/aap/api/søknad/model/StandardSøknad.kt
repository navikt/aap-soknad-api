package no.nav.aap.api.søknad.model

import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.oppslag.behandler.Behandler
import no.nav.aap.api.søknad.Ident
import java.time.LocalDate

data class StandardSøknad(
        val startdato: LocalDate,
        val kontaktinformasjon: Kontaktinformasjon,
        val behandlere: List<Behandler>,
        val utenlandsopphold: List<Utenlandsopphold> = emptyList(),
        val yrkesskadeType: YrkesskadeType,
        val utbetalinger: Utbetaling?,
        val arbeidsgiverGodtgjørelseType: ArbeidsgiverGodtgjørelseType?,
        val barn: List<Barn> = emptyList(),
        val tilleggsopplysninger: String?,
        val vedlegg: List<Vedlegg> = emptyList())

data class Kontaktinformasjon(val epost: String?, val telefonnummer: String?)

data class Utenlandsopphold(val arbeidet: Boolean  = true, val land: CountryCode, val periode: Periode)

enum class YrkesskadeType {
    JA,
    NEI,
    VET_IKKE
}

class Utbetaling(val stønadstyper: List<Stønadstype> = emptyList(),
        val godtgjørelseForVerv: Boolean = false,
                 val sluttpakke: Sluttpakke?,
        val utenlandskeYtelser: List<UtenlandskYtelse> = emptyList(),
        val andreUtbetalinger: List<AndreUtbetalinger> = emptyList(),
        val feriePeriode: Periode?)

enum class Stønadstype {
    KVALIFISERINGSSTØNAD,
    ØKONOMISK_SOSIALHJELP,
    INTRODUKSJONSSTØNAD,
    OMSORGSSTØNAD,
    FOSTERHJEMSGODTGJØRELSE,
    VERV,
    UTENLANDSK_TRYGD,
    ANNET
}

data class Sluttpakke(val type: ArbeidsgiverGodtgjørelseType, val beløp: Double)

class UtenlandskYtelse(val land: CountryCode,
        val ytelse: String)

class AndreUtbetalinger(
        val typeUtbetaling: String,
        val hvemUtbetaler: String)

enum class ArbeidsgiverGodtgjørelseType {
    ENGANGSBELØP,
    LØPENDE_UTBETALING
}

class BarnX(
        val ident: Fødselsnummer,
        val navn: Navn,
        val mottarBarnepensjon: Boolean = false,
        val harÅrligInntektOverGrunnbeløpet: Boolean = false,
        val bostedsland: String)

class Vedlegg