package no.nav.aap.api.felles

import java.time.LocalDate.now
import javax.validation.Validation
import javax.validation.constraints.Min
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class DomainTest {

    @Test
    @DisplayName("varighet viser riktige dager")
    fun varighet() {
        assertEquals(5, Periode(now(), now().plusDays(5)).varighetDager)
    }

    @Test
    fun email() {
        val validator = Validation.buildDefaultValidatorFactory().validator
        assertTrue(validator.validate(EmailTest(1)).isEmpty())
    }
    class EmailTest(@Min(2) val email: Int)
}