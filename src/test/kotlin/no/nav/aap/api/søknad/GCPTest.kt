package no.nav.aap.api.søknad

import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.jupiter.api.Test

class GCPTest {

    @Test
    fun convert() {
        print(LocalDateTime.ofEpochSecond(1666006038,0, ZoneOffset.UTC))
    }
}