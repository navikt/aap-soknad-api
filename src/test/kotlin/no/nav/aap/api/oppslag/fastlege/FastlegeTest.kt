package no.nav.aap.api.oppslag.fastlege

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.api.oppslag.fastlege.BehandlerType.FASTLEGE
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
            "      \"fnr\":\"01010111111\",\n" +
            "      \"fornavn\":\"Unni\",\n" +
            "      \"mellomnavn\":\"\",\n" +
            "      \"etternavn\":\"Larsen\",\n" +
            "      \"orgnummer\":\"976673867\",\n" +
            "      \"kontor\":\"Legesenteret AS\",\n" +
            "      \"adresse\":\"Legeveien 17\",\n" +
            "      \"postnummer\":\"5300\",\n" +
            "      \"poststed\":\"KLEPPESTØ\",\n" +
            "      \"telefon\":\"500000230\"\n" +
            "   }"

    @Test
    fun serdeserTest() {
        val o = OrgNummer("976673867")
        serdeser(o)
        val f = BehandlerDTO(FASTLEGE,"123", Fødselsnummer("11111111111"),"Unni", "Mellom","Larsen",
                OrgNummer("976673867"),"Kontor","Adresse","5300","KLEPPESTØ","61253479")
        serdeser(f,true)
    }
    private fun serdeser(a: Any, print: Boolean = false) {
        mapper.registerKotlinModule()
        mapper.registerModule(JavaTimeModule())
        val ser = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(a)
        if (print) println(ser)
        val deser = mapper.readValue(ser, a::class.java)
        if (print) println(deser)
        Assertions.assertThat(a).isEqualTo(deser)
    }
}