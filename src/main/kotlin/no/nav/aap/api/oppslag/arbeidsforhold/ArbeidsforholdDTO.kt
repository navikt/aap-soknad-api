package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.api.felles.Periode
import java.time.LocalDateTime

data class ArbeidsforholdDTO(
        val navArbeidsforholdId: String,
        val arbeidsforholdId: String,
        val arbeidstaker: Arbeidstaker,
        val arbeidsgiver: Arbeidsgiver,
        val opplysningspliktig: Opplysningspliktig,
        val type: String,
        val ansettelsesperiode : Ansettelsesperiode,
        val arbeidsavtaler: List<Arbeidsavtale>,
        val varsler: List<Varsel>,
        val innrapportertEtterAOrdningen: Boolean,
        val registrert: LocalDateTime,
        val sistBekreftet: LocalDateTime) {

    data class Arbeidstaker(val type: String,
                            val offentligIdent: String,
                            val aktoerId: String)

    data class Arbeidsgiver(val type: String,
                            val organisasjonsnummer: String)

    data class Varsel(val entitet: String,
                      val varslingskode: String)

    data class Opplysningspliktig(val type: String,
                                  val organisasjonsnummer: String)

    data class Ansettelsesperiode(val periode: Periode,
                                  val bruksperiode: Periode)

    data class Arbeidsavtale(val type: String,
                             val arbeidstidsordning: String,
                             val yrke: String,
                             val stillingsprosent: Double,
                             val antallTimerPrUke: Double,
                             val beregnetAntallTimerPrUke: Double,
                             val bruksperiode: Periode,
                             val gyldighetsperiode: Periode)

}