package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdDTO.Arbeidsforhold.Arbeidsavtale
import java.time.LocalDateTime

data class ArbeidsforholdDTO(
        val ansettelsesperiode : AnsettelsesperiodeDTO,
        val arbeidsavtaler: List<ArbeidsavtaleDTO>,
        val navArbeidsforholdId: String,
        val arbeidsforholdId: String,
        val arbeidstaker: ArbeidstakerDTO,
        val arbeidsgiver: ArbeidsgiverDTO,
        val opplysningspliktig: OpplysningspliktigDTO,
        val type: String,
        val varsler: List<VarselDTO>,
        val innrapportertEtterAOrdningen: Boolean,
        val registrert: LocalDateTime,
        val sistBekreftet: LocalDateTime) {

    data class ArbeidstakerDTO(val type: ArbeidstakerType,
                               val offentligIdent: String,
                               val aktoerId: String) {
        enum class ArbeidstakerType {
            Person
        }
    }

    data class ArbeidsgiverDTO(val type: ArbeidsgiverType,
                               val organisasjonsnummer: OrgNummer)

    enum class ArbeidsgiverType {
        Organisasjon,Person
    }
    data class VarselDTO(val entitet: String,
                         val varslingskode: String)

    data class OpplysningspliktigDTO(val type: ArbeidsgiverType,
                                     val organisasjonsnummer: OrgNummer)

    data class AnsettelsesperiodeDTO(val periode: Periode,
                                     val bruksperiode: Periode)

    data class ArbeidsavtaleDTO(val type: String,
                                val arbeidstidsordning: String,
                                val yrke: String,
                                val stillingsprosent: Double,
                                val antallTimerPrUke: Double,
                                val beregnetAntallTimerPrUke: Double,
                                val bruksperiode: Periode,
                                val gyldighetsperiode: Periode) {

         fun tilAvtale(periode: Periode)  = Arbeidsavtale(stillingsprosent,antallTimerPrUke, periode)
    }

    fun tilArbeidsforhold(orgNavn: String) = Arbeidsforhold(orgNavn, arbeidsavtaler.map{ it.tilAvtale(ansettelsesperiode.periode) })

    data class Arbeidsforhold(val navn: String,val avtaler: List<Arbeidsavtale>){
        data class Arbeidsavtale(val stillingsprosent: Double,val antallTimerPrUke: Double, val periode: Periode)
    }
}