package no.nav.aap.api.søknad.model

import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.oppslag.behandler.Behandler
import no.nav.aap.api.søknad.model.RadioValg.JA
import no.nav.aap.api.søknad.model.RadioValg.NEI
import no.nav.aap.api.søknad.model.RadioValg.VET_IKKE
import no.nav.aap.api.søknad.model.SøkerType.STUDENT
import no.nav.aap.api.søknad.model.SøkerType.VANLIG
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

data class StandardSøknad(
        val type: SøkerType = VANLIG,
        val startdato: LocalDate,
        val ferie: Ferie,
        val kontaktinformasjon: Kontaktinformasjon,
        val behandlere: List<Behandler>,
        val utenlandsopphold: List<Utenlandsopphold> = emptyList(),
        val yrkesskadeType: RadioValg,
        val utbetalinger: Utbetaling?,
        val arbeidsgiverGodtgjørelseType: ArbeidsgiverGodtgjørelseType?,
        val barn: List<BarnOgInntekt> = emptyList(),
        val tilleggsopplysninger: String?,
        val vedlegg: List<Vedlegg> = emptyList())

data class Kontaktinformasjon(val epost: String?, val telefonnummer: String?)

data class Utenlandsopphold(val arbeidet: Boolean  = true, val land: CountryCode, val periode: Periode)

data class Ferie(val periode: Periode? = null, val dager: Long? = null)  {
    constructor(dager: Long) : this(null,dager)
   constructor(periode: Periode) : this(periode, DAYS.between(periode.tom,periode.fom))
    val valgt: RadioValg = if (periode == null && dager == null) {
        VET_IKKE
    }
    else {
        if (dager == 0L){
            NEI
        }
        JA
    }
}
data class BarnOgInntekt(val barn: Barn, val inntekt: Inntekt)

enum class RadioValg { JA, NEI, VET_IKKE }

data class Inntekt(val inntekt: Double)

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

enum class SøkerType {
    STUDENT,VANLIG
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

class Vedlegg