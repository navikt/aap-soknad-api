package no.nav.aap.api.felles

import java.time.Duration
import java.time.LocalDate.now
import java.time.LocalDateTime
import java.time.LocalDateTime.*
import java.time.ZonedDateTime
import javax.validation.Validation
import javax.validation.constraints.Min
import kotlin.time.toKotlinDuration
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


    @Test
    fun dur() {
        val dur = Duration.between(ZonedDateTime.parse("2022-12-08T14:21:38.725Z").toLocalDateTime(),LocalDateTime.now()).toKotlinDuration()
        println("Slettet utlast etter $dur")
    }
}