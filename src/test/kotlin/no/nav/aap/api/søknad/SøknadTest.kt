package no.nav.aap.api.søknad

import com.fasterxml.jackson.databind.ObjectMapper
import com.neovisionaries.i18n.CountryCode.SE
import java.time.LocalDate.now
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.felles.PostNummer
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler.BehandlerKategori.LEGE
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler.BehandlerType.FASTLEGE
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler.KontaktInformasjon
import no.nav.aap.api.søknad.model.AnnetBarnOgInntekt
import no.nav.aap.api.søknad.model.AnnetBarnOgInntekt.Relasjon.FORELDER
import no.nav.aap.api.søknad.model.BarnOgInntekt
import no.nav.aap.api.søknad.model.Medlemskap
import no.nav.aap.api.søknad.model.RadioValg
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.StandardSøknad.Yrkesskade
import no.nav.aap.api.søknad.model.Studier
import no.nav.aap.api.søknad.model.Studier.StudieSvar.NEI
import no.nav.aap.api.søknad.model.Søker.Barn
import no.nav.aap.api.søknad.model.Utbetalinger
import no.nav.aap.api.søknad.model.Utbetalinger.AnnenStønad
import no.nav.aap.api.søknad.model.Utbetalinger.AnnenStønadstype.INTRODUKSJONSSTØNAD
import no.nav.aap.api.søknad.model.Utbetalinger.FraArbeidsgiver
import no.nav.aap.api.søknad.model.Utenlandsopphold
import no.nav.aap.api.søknad.model.Vedlegg
import no.nav.aap.util.AuthContext
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.json.JsonTest

@JsonTest
class SøknadTest {
    @Autowired
    lateinit var mapper: ObjectMapper

    @Mock
    lateinit var ctx: AuthContext

    val test = """
       {
          "startDato":{
             "fom":"2022-06-29"
          },
          "ferie":{
             "ferieType":"NEI"
          },
          "medlemsskap":{
             "boddINorgeSammenhengendeSiste5":true,
             "jobbetUtenforNorgeFørSyk":false,
             "utenlandsopphold":[
                
             ]
          },
          "studier":{
             "erStudent":"NEI"
          },
          "behandlere":[
             
          ],
          "yrkesskadeType":"NEI",
          "utbetalinger":{
             "ekstraFraArbeidsgiver":{
                "fraArbeidsgiver":false,
                "vedlegg":[
                   
                ]
             },
             "andreStønader":[
                {
                   "type":"OMSORGSSTØNAD",
                   "vedlegg":[
                      "823a5048-f6c4-464f-aaa1-a7e35846e382",
                      "e4845982-7033-4e98-97e2-98782b373488",
                      "e1abc29e-f926-43b9-812d-57260d3ed791"
                   ]
                }
             ]
          },
          "registrerteBarn":[
             {
                "fnr":"13012064629",
                "merEnnIG":false,
                "barnepensjon":false
             }
          ],
          "andreBarn":[
             
          ],
          "andreVedlegg":[
             
          ]
       }
       
        
    """.trimIndent()

    val bolk = """
     [
   {
      "ident":"13012064629",
      "code":"ok",
      "person":{
         "foedsel":[
            {
               "foedselsdato":"2020-01-13"
            }
         ],
         "navn":[
            {
               "fornavn":"LUGUBER",
               "mellomnavn":null,
               "etternavn":"GASELLE"
            }
         ],
         "adressebeskyttelse":[
            
         ],
         "doedsfall":[
            
         ]
      }
   }
]
        """
    val ettersending = """
    {
     "søknadId":"b86fdc45-6bbf-4891-98e8-5aed1247a301",
     "ettersendteVedlegg":[
        {
           "vedleggType":"ARBEIDSGIVER",
           "ettersending":[
              "5a58d38e-448b-4a62-84f9-e7700d3494aa"
            ]
         }
      ]
   }
        
    """.trimIndent()


    private fun LocalDateTime.asInstant() = atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toInstant()


    companion object {

        fun standardSøknad() = StandardSøknad(
                false,
                null,
                Studier(NEI, RadioValg.NEI),
                Medlemskap(true, null, null, null,
                        listOf(Utenlandsopphold(SE,
                                Periode(now(), now().plusDays(2)),
                                true, "11111111"))),
                listOf(RegistrertBehandler(FASTLEGE, LEGE, Navn("Lege", "A", "Legesen"),
                        KontaktInformasjon("Legekontoret",
                                OrgNummer("888888888"),
                                Adresse("Legegata", "17", "A",
                                        PostNummer("2600", "Lillehammer")),
                                "22222222"))), emptyList(),
                Yrkesskade.JA,
                Utbetalinger(
                        FraArbeidsgiver(true, Vedlegg(deler = listOf(UUID.randomUUID(),
                                UUID.randomUUID()))), listOf(AnnenStønad(INTRODUKSJONSSTØNAD))),
                "Tillegg",
                listOf(BarnOgInntekt(true)),
                listOf(AnnetBarnOgInntekt(Barn(Navn("Et", "ekstra", "Barn"), now().minusYears(14)),FORELDER,false,null,/*Vedlegg(deler = listOf(UUID.randomUUID()))*/)), Vedlegg(deler = listOf(UUID.randomUUID(),
                UUID.randomUUID())))
    }

    @SpringBootApplication
    internal class DummyApplication
}