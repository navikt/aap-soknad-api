package no.nav.aap.api

import java.time.LocalDate
import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.PostNummer
import no.nav.aap.api.søknad.model.Søker

object OMPersoner {

        fun ole_olsen() = Søker(Navn("Ole", "B", "Olsen"),
            Fødselsnummer("08089403198"),
            false,
            Adresse("Gata", "17", "A",
                PostNummer("2600", "Lillehammer")), LocalDate.now(), listOf(
                Søker.Barn(Navn("Barn", "B", "Barnsben"), LocalDate.now())
            ))

    fun har_barn() = Søker(
        Navn("Akseptabel", "", "Kveldsmat"),
        Fødselsnummer("07477222719"),
        false,
        Adresse("Gata", "17", "A",
            PostNummer("2600", "Lillehammer")
        ), LocalDate.now(), listOf(
            Søker.Barn(Navn("Livlig", "", "Sjokoladekake"), LocalDate.now())
        ))

    fun gradert_strengt_fortrolig() = Søker(
        Navn("Livlig", "", "Sjokoladekake"),
        Fødselsnummer("15457931856"),
        erBeskyttet=true,
        Adresse("Gata", "17", "A",
            PostNummer("2600", "Lillehammer"),
    ))

    fun midlertidig_oppholdstilatelse() = Søker(
        Navn("Søt", "", "Vogge"),
        Fødselsnummer("18829396285"),
        erBeskyttet=true,
        Adresse("Gata", "17", "A",
            PostNummer("2600", "Lillehammer"),
        ))

    fun har_barn_som_lever_under_strengt_fortrolig() = Søker(
        Navn("Tørr", "", "Journalist"),
        Fødselsnummer("19528500962"),
        erBeskyttet=true,
        Adresse("Gata", "17", "A",
            PostNummer("2600", "Lillehammer"),
        ))

    fun `skjermning har barn som lever under strengt fortrolig`() = Søker(
        Navn("Vårlig", "", "Hund"),
        Fødselsnummer("15428532039"),
        erBeskyttet=true,
        Adresse("Gata", "17", "A",
            PostNummer("2600", "Lillehammer"),
        ))

    fun skjerming() = Søker(
        Navn("Episk", "", "Svale"),
        Fødselsnummer("15428532039"),
        erBeskyttet=true,
        Adresse("Gata", "17", "A",
            PostNummer("2600", "Lillehammer"),
        ))

}