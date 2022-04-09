package no.nav.aap.api.søknad

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.api.oppslag.behandler.Behandler
import no.nav.aap.api.oppslag.behandler.Behandler.BehandlerType.FASTLEGE
import no.nav.aap.api.oppslag.behandler.Behandler.KontaktInformasjon
import no.nav.aap.api.søknad.model.ArbeidsgiverGodtgjørelseType.ENGANGSBELØP
import no.nav.aap.api.søknad.model.Barn
import no.nav.aap.api.søknad.model.BarnOgInntekt
import no.nav.aap.api.søknad.model.Ferie
import no.nav.aap.api.søknad.model.Inntekt
import no.nav.aap.api.søknad.model.Kontaktinformasjon
import no.nav.aap.api.søknad.model.RadioValg.JA
import no.nav.aap.api.søknad.model.RadioValg.VET_IKKE
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.SøkerType
import no.nav.aap.api.søknad.model.SøkerType.VANLIG
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import java.time.LocalDate
import java.time.LocalDate.now

@JsonTest
class BeriketTest {
    @Autowired
     lateinit var json: JacksonTester<StandardSøknadBeriket>
    @Autowired
    lateinit var barn: JacksonTester<BarnOgInntekt>

    @Test
    fun ferie(){
        assertThat(Ferie().valgt).isEqualTo(VET_IKKE)
    }
    @Test
    fun barn(){
        val b = BarnOgInntekt(Barn(Fødselsnummer("22222222222"),Navn("A","B","C"),LocalDate.now()),Inntekt(42.1))
        println(barn.write(b).json)
    }
    @Test
    fun fnr(){
       val s =  StandardSøknadBeriket(StandardSøknad(
               VANLIG,
               now(),
               Ferie(21),
                Kontaktinformasjon("a@b.com","22222222"),
                listOf(Behandler(FASTLEGE, Navn("Lege","A","Legesen"),
                        KontaktInformasjon("ref",
                                "kontor", OrgNummer("888888888"),
                                "gata",
                                "2600",
                                "Lillehammer",
                                "22222222"))),
                listOf(),
                JA,
                null,
                ENGANGSBELØP,
                listOf(),
                "JADDA",
                listOf()), Søker(
                Navn("A","B","C"),
                Fødselsnummer("03016536325"),null,
                LocalDate.now(),
                listOf()))
         val result = json.write(s)
        println(result.json)
    }
    @SpringBootApplication
     internal class DummyApplication
}