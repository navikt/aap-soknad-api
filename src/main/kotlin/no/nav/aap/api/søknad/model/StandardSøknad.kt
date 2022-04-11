package no.nav.aap.api.søknad.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.oppslag.behandler.Behandler
import no.nav.aap.api.søknad.model.RadioValg.JA
import no.nav.aap.api.søknad.model.RadioValg.NEI
import no.nav.aap.api.søknad.model.RadioValg.VET_IKKE
import no.nav.aap.api.søknad.model.SøkerType.STANDARD
import java.time.LocalDate

data class StandardSøknad(
        val type: SøkerType = STANDARD,
        val startdato: LocalDate,
        val ferie: Ferie,
        val medlemsskap: Medlemskap,
        val behandlere: List<Behandler>,
        val yrkesskadeType: RadioValg,
        val utbetalinger: Utbetaling?,
        val barn: List<BarnOgInntekt> = emptyList(),
        val tilleggsopplysninger: String?,
        val vedlegg: List<Vedlegg> = emptyList())

data class Medlemskap(val boddINorgeSamenhengendeSiste5: Boolean,
                      val jobbetUtenforNorgeFørSyk: Boolean,
                      val jobbetSammenhengendeINorgeSiste5: Boolean?,
                      val utenlandsopphold: List<Utenlandsopphold>)

data class Utenlandsopphold(val land: CountryCode, val periode: Periode, val arbeidet: Boolean, val id: String?)

data class Ferie(val periode: Periode? = null, val dager: Long? = null)  {
    constructor(dager: Long) : this(null,dager)
   constructor(periode: Periode) : this(periode,null)
    @JsonIgnore
    val valgt: RadioValg = if (periode == null && dager == null) {
        VET_IKKE
    }
    else {
        if (periode != null || (dager != null && dager > 0)){
            JA
        }
        else NEI
    }
}
data class BarnOgInntekt(val barn: Barn, val inntekt: Inntekt)

enum class RadioValg { JA, NEI, VET_IKKE }

data class Inntekt(val inntekt: Double)

class Utbetaling(val fraArbeidsgiver: Boolean,val stønadstyper: List<AnnenStønadstype> = emptyList())

enum class AnnenStønadstype {
    KVALIFISERINGSSTØNAD,
    ØKONOMISK_SOSIALHJELP,
    INTRODUKSJONSSTØNAD,
    OMSORGSSTØNAD,
    FOSTERHJEMSGODTGJØRELSE,
    VERV,
    UTENLANDSK_TRYGD,
    ANNET,
    INGEN
}

enum class SøkerType {
    STUDENT,STANDARD
}

class Vedlegg(vararg typer: String?) {

}