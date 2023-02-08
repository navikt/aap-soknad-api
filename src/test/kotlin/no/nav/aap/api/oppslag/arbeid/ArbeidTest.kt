package no.nav.aap.api.oppslag.arbeid

import no.nav.aap.api.felles.MockWebServerExtensions.expect
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

class ArbeidTest {

    lateinit var arbeidServer: MockWebServer
    lateinit var orgServer: MockWebServer

    lateinit var client: ArbeidClient

    var arbeid = """     [ {
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
    """

    @BeforeEach
    fun beforeEach() {
        arbeidServer = MockWebServer()
        orgServer = MockWebServer()
        val arbeidCfg = ArbeidConfig(arbeidServer.url("/").toUri())
        val arbeidAdapter = ArbeidWebClientAdapter(WebClient.builder().baseUrl("${arbeidCfg.baseUri}").build(),arbeidCfg)
        val orgCfg = OrganisasjonConfig(orgServer.url("/").toUri())
        val orgAdapter = OrganisasjonWebClientAdapter(WebClient.builder().baseUrl("${orgCfg.baseUri}").build(),orgCfg)
        client = ArbeidClient(arbeidAdapter,orgAdapter)
        }

    @Test
    fun ok() {
        arbeidServer.expect(arbeid)
    }
}