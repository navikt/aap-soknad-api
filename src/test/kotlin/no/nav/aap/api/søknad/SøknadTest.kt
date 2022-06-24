package no.nav.aap.api.søknad

import com.fasterxml.jackson.databind.ObjectMapper
import com.neovisionaries.i18n.CountryCode.SE
import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.felles.PostNummer
import no.nav.aap.api.oppslag.behandler.Behandler
import no.nav.aap.api.oppslag.behandler.Behandler.BehandlerKategori.LEGE
import no.nav.aap.api.oppslag.behandler.Behandler.BehandlerType.FASTLEGE
import no.nav.aap.api.oppslag.behandler.Behandler.KontaktInformasjon
import no.nav.aap.api.søknad.model.AnnetBarnOgInntekt
import no.nav.aap.api.søknad.model.BarnOgInntekt
import no.nav.aap.api.søknad.model.Ferie
import no.nav.aap.api.søknad.model.Ferie.FerieType.DAGER
import no.nav.aap.api.søknad.model.Medlemskap
import no.nav.aap.api.søknad.model.RadioValg
import no.nav.aap.api.søknad.model.RadioValg.JA
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Startdato
import no.nav.aap.api.søknad.model.Startdato.Hvorfor.HELSE
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
import no.nav.aap.api.søknad.model.V
import no.nav.aap.api.søknad.model.Vedlegg
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.json.JsonTest
import java.time.LocalDate.now
import java.util.*
import kotlin.test.assertEquals

@JsonTest
class SøknadTest {
    @Autowired
    lateinit var mapper: ObjectMapper

    @Test
    fun serialize() {
        var orig = standardSøknad()
        val s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(orig)
        print(s)
        var ss = mapper.readValue(s, StandardSøknad::class.java)
        assertEquals(orig, ss)
    }

    private fun søker(): Søker {
        return Søker(Navn("Ole", "B", "Olsen"),
                Fødselsnummer("01010111111"),
                Adresse("Gata", "17", "A",
                        PostNummer("2600", "Lillehammer")), now(), listOf(
                Barn(Fødselsnummer("22222222222"),
                        Navn("Barn", "B", "Barnsben"), now())))
    }

    fun standardSøknad() = StandardSøknad(
            Studier(NEI, RadioValg.NEI),
            Startdato(now(), HELSE, "Noe annet"),
            Ferie(DAGER, dager = 20),
            Medlemskap(true, null, null, null,
                    listOf(Utenlandsopphold(SE,
                            Periode(now(), now().plusDays(2)),
                            true, "11111111"))),
            listOf(Behandler(FASTLEGE, LEGE, Navn("Lege", "A", "Legesen"),
                    KontaktInformasjon("Legekontoret",
                            OrgNummer("888888888"),
                            Adresse("Legegata", "17", "A",
                                    PostNummer("2600", "Lillehammer")),
                            "22222222"))),
            JA,
            Utbetaling(FraArbeidsgiver(true, V(listOf(UUID.randomUUID(),
                    UUID.randomUUID()), "ARBEIDSGIVER")), listOf(AnnenStønad(INTRODUKSJONSSTØNAD)),
                    EkstraUtbetaling("hvilken", "hvem")),
            listOf(BarnOgInntekt(Fødselsnummer("08089403198"), merEnnIG = true, barnepensjon = false)),
            listOf(AnnetBarnOgInntekt(Barn(Fødselsnummer("08089403198"),
                    Navn("Et", "ekstra", "Barn"), now().minusYears(14)), vedlegg = V(tittel = "BARN"))),
            "Tilegg", listOf(vedlegg("Andre"), vedlegg("Andre")))

    private fun vedlegg(tittel: String) = Vedlegg(V(tittel = tittel))

    @SpringBootApplication
    internal class DummyApplication
}