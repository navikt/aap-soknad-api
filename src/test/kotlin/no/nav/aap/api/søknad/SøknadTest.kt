package no.nav.aap.api.søknad

import com.fasterxml.jackson.databind.ObjectMapper
import com.neovisionaries.i18n.CountryCode.SE
import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.felles.PostNummer
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler.BehandlerKategori.LEGE
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler.BehandlerType.FASTLEGE
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler.KontaktInformasjon
import no.nav.aap.api.søknad.model.AnnetBarnOgInntekt
import no.nav.aap.api.søknad.model.BarnOgInntekt
import no.nav.aap.api.søknad.model.Ferie
import no.nav.aap.api.søknad.model.Ferie.FerieType.DAGER
import no.nav.aap.api.søknad.model.Medlemskap
import no.nav.aap.api.søknad.model.RadioValg
import no.nav.aap.api.søknad.model.RadioValg.JA
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Startdato
import no.nav.aap.api.søknad.model.Studier
import no.nav.aap.api.søknad.model.Studier.StudieSvar.NEI
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.Søker.Barn
import no.nav.aap.api.søknad.model.Utbetaling
import no.nav.aap.api.søknad.model.Utbetaling.AnnenStønad
import no.nav.aap.api.søknad.model.Utbetaling.AnnenStønadstype.INTRODUKSJONSSTØNAD
import no.nav.aap.api.søknad.model.Utbetaling.EkstraUtbetaling
import no.nav.aap.api.søknad.model.Utbetaling.FraArbeidsgiver
import no.nav.aap.api.søknad.model.Utenlandsopphold
import no.nav.aap.api.søknad.model.Vedlegg
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.json.JsonTest
import java.time.LocalDate.now
import java.util.*

@JsonTest
class SøknadTest {
    @Autowired
    lateinit var mapper: ObjectMapper

    val v2 = """
        [ "07e35799-4db5-4ba3-81cc-3681dd1dec60" ]
    """.trimIndent()
    val v3 = """
         "07e35799-4db5-4ba3-81cc-3681dd1dec60" 
    """.trimIndent()

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

    companion object {

        fun søker(): Søker {
            return Søker(Navn("Ole", "B", "Olsen"),
                    Fødselsnummer("08089403198"),
                    Adresse("Gata", "17", "A",
                            PostNummer("2600", "Lillehammer")), now(), listOf(
                    Barn(Navn("Barn", "B", "Barnsben"), now())))
        }

        fun standardSøknad() = StandardSøknad(
                Studier(NEI, RadioValg.NEI),
                Startdato("Noe annet"),
                Ferie(DAGER, dager = 20),
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
                JA,
                Utbetaling(
                        FraArbeidsgiver(true, Vedlegg(deler = listOf(UUID.randomUUID(),
                                UUID.randomUUID()))), listOf(AnnenStønad(INTRODUKSJONSSTØNAD)),
                        EkstraUtbetaling("hvilken", "hvem")),
                listOf(BarnOgInntekt(merEnnIG = true, barnepensjon = false)),
                listOf(AnnetBarnOgInntekt(Barn(Navn("Et", "ekstra", "Barn"), now().minusYears(14)))),
                "Tilegg", Vedlegg(deler = listOf(UUID.randomUUID(),
                UUID.randomUUID())))
    }

    @SpringBootApplication
    internal class DummyApplication
}