package no.nav.aap.api.søknad

import com.neovisionaries.i18n.CountryCode.SE
import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.felles.PostNummer
import no.nav.aap.api.oppslag.behandler.Behandler
import no.nav.aap.api.oppslag.behandler.Behandler.BehandlerType.FASTLEGE
import no.nav.aap.api.oppslag.behandler.Behandler.KontaktInformasjon
import no.nav.aap.api.søknad.model.Barn
import no.nav.aap.api.søknad.model.BarnOgInntekt
import no.nav.aap.api.søknad.model.Ferie
import no.nav.aap.api.søknad.model.Inntekt
import no.nav.aap.api.søknad.model.Medlemskap
import no.nav.aap.api.søknad.model.RadioValg.JA
import no.nav.aap.api.søknad.model.RadioValg.NEI
import no.nav.aap.api.søknad.model.RadioValg.VET_IKKE
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.SøkerType.STANDARD
import no.nav.aap.api.søknad.model.Utbetaling
import no.nav.aap.api.søknad.model.Utbetaling.AnnenStønad
import no.nav.aap.api.søknad.model.Utbetaling.AnnenStønadstype.FOSTERHJEMSGODTGJØRELSE
import no.nav.aap.api.søknad.model.Utbetaling.AnnenUtbetaling
import no.nav.aap.api.søknad.model.Utenlandsopphold
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import java.time.LocalDate.now
import java.util.UUID

@JsonTest
class BeriketTest {
    @Autowired
    lateinit var json: JacksonTester<StandardSøknad>
    @Autowired
    lateinit var barn: JacksonTester<BarnOgInntekt>
    @Autowired
    lateinit var ferie: JacksonTester<Ferie>

    @Test
    fun ferie(){
        assertThat(Ferie().valgt).isEqualTo(VET_IKKE)
        assertThat(Ferie(20).valgt).isEqualTo(JA)
        assertThat(Ferie(0).valgt).isEqualTo(NEI)
        assertThat(Ferie(Periode(now(),now().plusDays(1))).valgt).isEqualTo(JA)
    }
    @Test
    fun barn(){
        val b = BarnOgInntekt(Barn(Fødselsnummer("22222222222"),Navn("A","B","C"),now()),Inntekt(42.1))
        println(barn.write(b).json)
    }
    @Test
    fun søknad(){
       val s =  StandardSøknad(STANDARD,
               now(),
               Ferie(21),
               Medlemskap(true,
                       false,
                       null,
                       listOf(Utenlandsopphold(SE,
                               Periode(now(),
                                       now().plusDays(2)),
                               true,
                               "11111111"))),
                listOf(Behandler(FASTLEGE,
                        Navn("Lege",
                                "A",
                                "Legesen"),
                        KontaktInformasjon("ref",
                                "Legekontoret",
                                OrgNummer("888888888"),
                                Adresse("Legegata",
                                        null,null,
                                        PostNummer("2600", "Lillehammer")),
                                "22222222"))),
                JA,
               Utbetaling(false,
                       listOf(AnnenStønad(FOSTERHJEMSGODTGJØRELSE,UUID.randomUUID())),
                       listOf(AnnenUtbetaling("hvilken",
                               "hvem"))),
                listOf(BarnOgInntekt(
                        Barn(Fødselsnummer("22222222"),
                                Navn("Et",
                                        "lite",
                                        "Barn"),
                        now().minusYears(14)),
                        Inntekt(42.5))),
               "Tilegg")
        println(json.write(s).json)
    }
    @SpringBootApplication
     internal class DummyApplication
}