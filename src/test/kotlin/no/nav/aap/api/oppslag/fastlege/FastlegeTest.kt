package no.nav.aap.api.oppslag.fastlege

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.aap.api.felles.OrgNummer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [ObjectMapper::class])
class FastlegeTest {
    @Autowired
    lateinit var mapper: ObjectMapper
    val json = "[\n" +
            "   {\n" +
            "      \"type\":\"FASTLEGE\",\n" +
            "      \"behandlerRef\":\"d182f24b-ebca-4f44-bf86-65901ec6141b\",\n" +
            "      \"fnr\":\"01117302624\",\n" +
            "      \"fornavn\":\"Nina Unni\",\n" +
            "      \"mellomnavn\":\"\",\n" +
            "      \"etternavn\":\"Borge\",\n" +
            "      \"orgnummer\":\"976673867\",\n" +
            "      \"kontor\":\"ASKØY KOMMUNE SAMFUNNSMEDISINSK AVD ALMENNLEGETJENESTEN\",\n" +
            "      \"adresse\":\"Kleppeveien 17\",\n" +
            "      \"postnummer\":\"5300\",\n" +
            "      \"poststed\":\"KLEPPESTØ\",\n" +
            "      \"telefon\":\"500000230\"\n" +
            "   }"

    @Test
    fun serdeserTest() {
        val o = OrgNummer("976673867")
        serdeser(o)
    }
    private fun serdeser(a: Any, print: Boolean = false) {
        mapper.registerKotlinModule()
        mapper.registerModule(JavaTimeModule())
        val ser = mapper.writeValueAsString(a)
        if (print) println(ser)
        val deser = mapper.readValue(ser, a::class.java)
        if (print) println(deser)
        Assertions.assertThat(a).isEqualTo(deser)
    }
}