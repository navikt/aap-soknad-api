package no.nav.aap.api.søknad.minside

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.minside.MinSideForside.EventName.enable
import org.junit.jupiter.api.Test

class MinSideTests {

    @Test
    fun test() {
        println(jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(MinSideForside(enable, Fødselsnummer("25894898405"))))
    }
}