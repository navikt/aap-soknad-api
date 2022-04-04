package no.nav.aap.api.søknad.model

import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.søknad.Ident

class FullSøknad(
        val ident: Ident,
        val navn: Navn,
        val adresse: Adresse,
        val kontaktinformasjon: Kontaktinformasjon,
        val leger: List<Lege>,
        val utenlandsopphold: List<Utenlandsopphold> = emptyList(),
        val arbeidUtland: List<ArbeidUtland> = emptyList(),
        val yrkesskadeType: YrkesskadeType,
        val utbetalinger: Utbetaling?,
        val arbeidsgiverGodtgjørelseType: ArbeidsgiverGodtgjørelseType?,
        val barn: List<Barn> = emptyList(),
        val tilleggsopplysninger: String?,
        val vedlegg: List<Vedlegg> = emptyList())

class Adresse(
        val erFolkeregistrert: Boolean,
        val gateadresse: String,
        val postnummer: String?,
        val poststed: String,
        val land: String?)

class Kontaktinformasjon(
        val epost: String?,
        val telefonnummer: String?)

class Lege(
        val rolle: Rolle,
        val navn: Navn,
        val gateadresse: String,
        val postnummer: String,
        val poststed: String,
        val telefonnummer: String) {
    enum class Rolle {
        FASTLEGE,
        BEHANDLER
    }
}

class Utenlandsopphold(
        val land: CountryCode,
        val periode: Periode)

class ArbeidUtland(
        val land: CountryCode,
        val periode: Periode)

enum class YrkesskadeType {
    GODKJENT_AV_NAV,
    IKKE_GODKJENT_AV_NAV,
    SØKNAD,
    VET_IKKE
}

class Utbetaling(
        val stønadstyper: List<Stønadstype> = emptyList(),
        val godtgjørelseForVerv: Boolean = false,
        val utenlandskeYtelser: List<UtenlandskYtelse> = emptyList(),
        val andreUtbetalinger: List<AndreUtbetalinger> = emptyList(),
        val feriePeriode: Periode?)

enum class Stønadstype {
    KVALIFISERINGSSTØNAD,
    ØKONOMISK_SOSIALHJELP,
    INTRODUKSJONSSTØNAD,
    OMSORGSSTØNAD,
    FOSTERHJEMSGODTGJØRELSE
}

class UtenlandskYtelse(
        val land: String,
        val ytelse: String)

class AndreUtbetalinger(
        val typeUtbetaling: String,
        val hvemUtbetaler: String)

enum class ArbeidsgiverGodtgjørelseType {
    ENGANGSBELØP,
    LØPENDE_UTBETALING
}

class Barn(
        val ident: Ident,
        val navn: Navn,
        val mottarBarnepensjon: Boolean = false,
        val harÅrligInntektOverGrunnbeløpet: Boolean = false,
        val bostedsland: String
          )

class Vedlegg