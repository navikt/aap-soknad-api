package no.nav.aap.api.oppslag.arbeidsforhold

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsgiverType.Organisasjon
import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsgiverType.Person
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest(classes = [ObjectMapper::class])
class ArbeidTest {
    @Autowired
    lateinit var mapper: ObjectMapper
   val json = " [\n" +
           "    {\n" +
           "        \"navArbeidsforholdId\":9602,\n" +
           "        \"arbeidsforholdId\":\"1\",\n" +
           "        \"arbeidstaker\":{\n" +
           "        \"type\":\"Person\",\n" +
           "        \"offentligIdent\":\"08089403198\",\n" +
           "        \"aktoerId\":\"2187363752271\"\n" +
           "    },\n" +
           "        \"arbeidsgiver\":{\n" +
           "        \"type\":\"Organisasjon\",\n" +
           "        \"organisasjonsnummer\":\"947064649\"\n" +
           "    },\n" +
           "        \"opplysningspliktig\":{\n" +
           "        \"type\":\"Organisasjon\",\n" +
           "        \"organisasjonsnummer\":\"928497704\"\n" +
           "    },\n" +
           "        \"type\":\"ordinaertArbeidsforhold\",\n" +
           "        \"ansettelsesperiode\":{\n" +
           "        \"periode\":{\n" +
           "        \"fom\":\"2001-11-05\"\n" +
           "    },\n" +
           "        \"bruksperiode\":{\n" +
           "        \"fom\":\"2022-01-13T14:50:36.243\"\n" +
           "    }\n" +
           "    },\n" +
           "        \"arbeidsavtaler\":[\n" +
           "        {\n" +
           "            \"type\":\"Ordinaer\",\n" +
           "            \"arbeidstidsordning\":\"ikkeSkift\",\n" +
           "            \"yrke\":\"2521106\",\n" +
           "            \"stillingsprosent\":100.0,\n" +
           "            \"antallTimerPrUke\":37.5,\n" +
           "            \"beregnetAntallTimerPrUke\":37.5,\n" +
           "            \"bruksperiode\":{\n" +
           "            \"fom\":\"2022-01-13T14:50:36.243\"\n" +
           "        },\n" +
           "            \"gyldighetsperiode\":{\n" +
           "            \"fom\":\"2001-11-01\"\n" +
           "        }\n" +
           "        }\n" +
           "        ],\n" +
           "        \"varsler\":[\n" +
           "        {\n" +
           "            \"entitet\":\"ARBEIDSFORHOLD\",\n" +
           "            \"varslingskode\":\"NAVEND\"\n" +
           "        }\n" +
           "        ],\n" +
           "        \"innrapportertEtterAOrdningen\":true,\n" +
           "        \"registrert\":\"2022-01-13T14:50:36.146\",\n" +
           "        \"sistBekreftet\":\"2022-01-13T14:50:36\"\n" +
           "    }\n" +
           "    ]"

    @Test
    fun serdeserTest() {
       val a1 = ArbeidsgiverDTO(Organisasjon, OrgNummer("999263550"))
        serdeser(a1)
        val p = Periode(LocalDate.now(),LocalDate.now().plusDays(1))
        serdeser(p)
        val ap = AnsettelsesperiodeDTO(p)
        serdeser(ap)
        val aa = ArbeidsavtaleDTO(100.0,37.5)
        serdeser(aa)
        val af = ArbeidsforholdDTO(ap,listOf(aa),a1)
        serdeser(af)
    }

    private fun serdeser(a: Any, print: Boolean = false) {
        mapper.registerKotlinModule()
        mapper.registerModule(JavaTimeModule())
        val ser = mapper.writeValueAsString(a)
        if (print) println(ser)
        val deser = mapper.readValue(ser, a::class.java)
        if (print) println(deser)
        assertThat(a).isEqualTo(deser)
    }
}