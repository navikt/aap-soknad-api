package no.nav.aap.api.oppslag.krr

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.Email
import no.nav.aap.api.oppslag.krr.Kontaktinformasjon.Companion.EMPTY
import no.nav.aap.util.LoggerUtil.getLogger

@JsonIgnoreProperties(ignoreUnknown = true)
data class KontaktinformasjonDTO(@JsonAlias("spraak") val målform: Målform? = Målform.NB,
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
                        log.trace("Kan ikke varsles, kanVarsles er false i KRR")
                        EMPTY
                    }
                }
            }

            else -> {
                log.trace("Er ikke aktiv i KRR")
                EMPTY
            }
        }
}

data class Kontaktinformasjon(val epost: String? = null, val mobil: String? = null) {
    companion object {
        val EMPTY = Kontaktinformasjon()
    }
}

enum class Målform { NB, NN, EN }