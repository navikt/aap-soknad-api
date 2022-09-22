package no.nav.aap.api.oppslag.arbeid

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.oppslag.arbeid.ArbeidsforholdDTO.AnsettelsesperiodeDTO
import no.nav.aap.api.oppslag.arbeid.ArbeidsforholdDTO.ArbeidsavtaleDTO
import no.nav.aap.api.oppslag.arbeid.ArbeidsforholdDTO.ArbeidsgiverDTO
import no.nav.aap.api.oppslag.arbeid.ArbeidsforholdDTO.ArbeidsgiverDTO.ArbeidsgiverType.Organisasjon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest(classes = [ObjectMapper::class])
class ArbeidTest {
    @Autowired
    lateinit var mapper: ObjectMapper
    var json = """     [ {
            "type":"Ordinaer",
            "arbeidstidsordning":"ikkeSkift",
            "yrke":"2521106",
            "stillingsprosent":100.0,
            "antallTimerPrUke":37.5,
            "beregnetAntallTimerPrUke":37.5,
            "bruksperiode":{
               "fom":"2022-01-13T14:50:36.243"
            },
            "gyldighetsperiode":{
               "fom":"2001-11-01"
            }
         }
      ],
      "varsler":[
         {
            "entitet":"ARBEIDSFORHOLD",
            "varslingskode":"NAVEND"
         }
      ],
      "innrapportertEtterAOrdningen":true,
      "registrert":"2022-01-13T14:50:36.146",
      "sistBekreftet":"2022-01-13T14:50:36"
   }
]
    """.trimIndent()
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