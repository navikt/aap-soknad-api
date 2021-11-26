package no.nav.aap.api.felles

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

}
