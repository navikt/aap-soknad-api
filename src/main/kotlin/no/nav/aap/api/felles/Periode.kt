package no.nav.aap.api.felles

import java.time.Duration
import java.time.LocalDate

data class Periode(val fom: LocalDate, val tom: LocalDate?) {

    fun varighetDager(): Long {
        return if(tom == null) {
            -1
        } else {
            Duration.between(fom, tom).toDays()
        }
    }

}
