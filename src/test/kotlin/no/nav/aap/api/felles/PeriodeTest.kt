package no.nav.aap.api.felles

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.api.oppslag.pdl.PDLKjønn
import no.nav.aap.api.oppslag.pdl.PDLKjønn.Kjoenn.KVINNE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PeriodeTest {

    @Test
    fun `varighet viser riktige dager`() {
        val now = LocalDate.now()
        val then = now.plusDays(5)
        val periode = Periode(now, then)

        assertEquals(5, periode.varighetDager())
    }
    @Test
    fun map() {
        print(ObjectMapper().writeValueAsString(PDLKjønn(KVINNE)))
    }
}