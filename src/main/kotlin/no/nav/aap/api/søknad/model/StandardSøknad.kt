package no.nav.aap.api.søknad.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.oppslag.behandler.Behandler
import no.nav.aap.api.søknad.model.RadioValg.JA
import no.nav.aap.api.søknad.model.RadioValg.NEI
import no.nav.aap.api.søknad.model.RadioValg.VET_IKKE
import no.nav.aap.api.søknad.model.SøkerType.STANDARD
import no.nav.aap.joark.DokumentVariant
import no.nav.aap.joark.Filtype.JSON
import no.nav.aap.joark.VariantFormat.ORIGINAL
import no.nav.aap.util.StringExtensions.toEncodedJson
import java.time.LocalDate
import java.util.*

data class StandardSøknad(
        val type: SøkerType = STANDARD,
        val startdato: Startdato,
        val ferie: Ferie,
        val medlemsskap: Medlemskap,
        val behandlere: List<Behandler>,
        val yrkesskadeType: RadioValg,
        val utbetalinger: Utbetaling?,
        val registrerteBarn: List<BarnOgInntekt> = emptyList(),
        val andreBarn: List<BarnOgInntekt> = emptyList(),
        val tilleggsopplysninger: String?,
        val andreVedlegg: List<UUID> = emptyList()) {

    fun asJsonVariant(mapper: ObjectMapper) = DokumentVariant(JSON, this.toEncodedJson(mapper), ORIGINAL)

}

data class Startdato(val fom: LocalDate, val hvorfor: HvorforTilbake?, val beskrivelse: String?) {
    enum class HvorforTilbake {
        HELSE,
        FEILINFO
    }
}

data class Medlemskap(val boddINorgeSammenhengendeSiste5: Boolean,
                      val jobbetUtenforNorgeFørSyk: Boolean,
                      val jobbetSammenhengendeINorgeSiste5: Boolean?,
                      val utenlandsopphold: List<Utenlandsopphold>)

class Utenlandsopphold private constructor(val land: CountryCode,
                                           val landNavn: String,
                                           val periode: Periode,
                                           val arbeidet: Boolean,
                                           val id: String?) {
    @JsonCreator
    constructor(land: CountryCode, periode: Periode, arbeidet: Boolean, id: String?) : this(
            land,
            land.toLocale().displayCountry,
            periode,
            arbeidet,
            id)
}

data class Ferie(val periode: Periode? = null, val dager: Long? = null) {
    constructor(dager: Long) : this(null, dager)
    constructor(periode: Periode) : this(periode, null)

    @JsonIgnore
    val valgt: RadioValg = if (periode == null && dager == null) {
        VET_IKKE
    }
    else {
        if (periode != null || (dager != null && dager > 0)) {
            JA
        }
        else NEI
    }
}

data class BarnOgInntekt(val barn: Barn, val merEnnIG: Boolean = false, val barnepensjon: Boolean = false)

enum class RadioValg {
    JA,
    NEI,
    VET_IKKE
}

class Utbetaling(val fraArbeidsgiver: Boolean,
                 val stønadstyper: List<AnnenStønad> = emptyList(),
                 val andreUtbetalinger: List<AnnenUtbetaling>) {
    data class AnnenUtbetaling(val hvilken: String, val hvem: String, override val vedlegg: UUID? = null) : VedleggAware

    data class AnnenStønad(val type: AnnenStønadstype, override val vedlegg: UUID? = null) : VedleggAware

    interface VedleggAware {
        val vedlegg: UUID?
    }

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
}

enum class SøkerType {
    STUDENT,
    STANDARD
}