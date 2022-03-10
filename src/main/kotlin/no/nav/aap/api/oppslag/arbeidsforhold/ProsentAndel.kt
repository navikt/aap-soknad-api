package no.nav.aap.api.oppslag.arbeidsforhold

import com.fasterxml.jackson.annotation.JsonValue


data class ProsentAndel(private val p: Number) {
    @JsonValue
     val prosent: Double = p.toDouble()

}