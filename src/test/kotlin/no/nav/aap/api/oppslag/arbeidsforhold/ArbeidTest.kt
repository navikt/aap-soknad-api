package no.nav.aap.api.oppslag.arbeidsforhold

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdDTO.Ansettelsesperiode
import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdDTO.Arbeidsavtale
import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdDTO.Arbeidsgiver
import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdDTO.Arbeidstaker
import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdDTO.Opplysningspliktig
import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdDTO.Varsel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalDateTime

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
        val a = Arbeidstaker("123", "456", aktoerId = "789")
        serdeser(a)
        val a1 = Arbeidsgiver("123", OrgNummer("999263550"))
        serdeser(a1)
        val v = Varsel("entitet","kode")
        serdeser(v)
        val o = Opplysningspliktig("ja",OrgNummer("999263550"))
        serdeser(o)
        val p = Periode(LocalDate.now(),LocalDate.now().plusDays(1))
        serdeser(p)
        val ap = Ansettelsesperiode(p,p)
        serdeser(ap)
        var aa = Arbeidsavtale("type","ordning","sjef",100.0,37.5,37.5,p,p)
        serdeser(aa)
        var af = ArbeidsforholdDTO("1","2",a,a1,o,"type",ap,listOf(aa),listOf(v),true, LocalDateTime.now(),LocalDateTime.now())
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