package no.nav.aap.api.oppslag.krr

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.aap.util.LoggerUtil.getLogger
import javax.validation.constraints.Email

@JsonIgnoreProperties(ignoreUnknown = true)
data class KontaktinformasjonDTO(@JsonAlias("spraak") val målform: Målform? = Målform.standard(),
                                 val reservert: Boolean? = null,
                                 val kanVarsles: Boolean? = false,
                                 val aktiv: Boolean? = false,
                                 @JsonAlias("epostadresse") @Email val epost: String? = null,
                                 @JsonAlias("mobiltelefonnummer") val mobil: String? = null) {

    private val log = getLogger(javaClass)
    fun tilKontaktinfo() =
        when (aktiv) {
            true -> {
                when (kanVarsles) {
                    true -> Kontaktinformasjon(epost, mobil)
                    else -> {
                        log.info("Kan ikke varsles, kanVarsles er false i KRR")
                        null
                    }
                }
            }

            else -> {
                log.info("Er ikke aktiv i KRR")
                null
            }
        }
}

data class Kontaktinformasjon(val epost: String? = null, val mobil: String? = null)

enum class Målform {
    NB,
    NN,
    EN;

    companion object {
        fun standard() = NB
    }
}