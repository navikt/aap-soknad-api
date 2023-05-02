package no.nav.aap.api

import com.neovisionaries.i18n.CountryCode.SE
import java.time.LocalDate
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
import no.nav.aap.api.søknad.fordeling.AnnetBarnOgInntekt
import no.nav.aap.api.søknad.fordeling.AnnetBarnOgInntekt.Relasjon.FORELDER
import no.nav.aap.api.søknad.fordeling.BarnOgInntekt
import no.nav.aap.api.søknad.fordeling.Medlemskap
import no.nav.aap.api.søknad.fordeling.RadioValg
import no.nav.aap.api.søknad.fordeling.StandardSøknad
import no.nav.aap.api.søknad.fordeling.StandardSøknad.Yrkesskade.JA
import no.nav.aap.api.søknad.fordeling.Studier
import no.nav.aap.api.søknad.fordeling.Studier.StudieSvar.AVBRUTT
import no.nav.aap.api.søknad.fordeling.Studier.StudieSvar.NEI
import no.nav.aap.api.søknad.fordeling.Utbetalinger
import no.nav.aap.api.søknad.fordeling.Utbetalinger.AnnenStønad
import no.nav.aap.api.søknad.fordeling.Utbetalinger.AnnenStønadstype.INTRODUKSJONSSTØNAD
import no.nav.aap.api.søknad.fordeling.Utbetalinger.FraArbeidsgiver
import no.nav.aap.api.søknad.fordeling.Utenlandsopphold
import no.nav.aap.api.søknad.fordeling.Vedlegg
import no.nav.aap.api.oppslag.person.Søker.Barn

object OMSøknad {

        fun standard_soknad() = StandardSøknad(
            false,
            null,
            Studier(NEI, RadioValg.NEI),
            Medlemskap(true, null, null, null,
                listOf(
                    Utenlandsopphold(
                        SE,
                    Periode(LocalDate.now(), LocalDate.now().plusDays(2)),
                    true, "11111111")
                )),
            listOf(
                RegistrertBehandler(
                    FASTLEGE, LEGE, Navn("Lege", "A", "Legesen"),
                KontaktInformasjon("Legekontoret",
                    OrgNummer("888888888"),
                    Adresse("Legegata", "17", "A",
                        PostNummer("2600", "Lillehammer")
                    ),
                    "22222222")
                )
            ), emptyList(),
            JA,
            Utbetalinger(
                FraArbeidsgiver(true, Vedlegg(deler = listOf(
                    UUID.randomUUID(),
                    UUID.randomUUID()))
                ), listOf(AnnenStønad(INTRODUKSJONSSTØNAD))),
            "Tillegg",
            listOf(BarnOgInntekt(true)),
            listOf(
                AnnetBarnOgInntekt(
                    Barn(Navn("Et", "ekstra", "Barn"), LocalDate.now().minusYears(14)),
                    FORELDER,false,
                    Vedlegg(deler = listOf(UUID.randomUUID()))
                )
            ), Vedlegg(deler = listOf(
                UUID.randomUUID(),
                UUID.randomUUID()))
                                              )

        fun er_student_søknad() = StandardSøknad(
            false,
            null,
            Studier(AVBRUTT, RadioValg.JA),
            Medlemskap(true, null, null, null,
                listOf(
                    Utenlandsopphold(
                        SE,
                        Periode(LocalDate.now(), LocalDate.now().plusDays(2)),
                        true, "11111111")
                )),
            listOf(
                RegistrertBehandler(
                    FASTLEGE, LEGE, Navn("Lege", "A", "Legesen"),
                    KontaktInformasjon("Legekontoret",
                        OrgNummer("888888888"),
                        Adresse("Legegata", "17", "A",
                            PostNummer("2600", "Lillehammer")
                        ),
                        "22222222")
                )
            ), emptyList(),
            JA,
            Utbetalinger(
                FraArbeidsgiver(true, Vedlegg(deler = listOf(
                    UUID.randomUUID(),
                    UUID.randomUUID()))
                ), listOf(AnnenStønad(INTRODUKSJONSSTØNAD))),
            "Tillegg",
            listOf(BarnOgInntekt(true)),
            listOf(
                AnnetBarnOgInntekt(
                    Barn(Navn("Et", "ekstra", "Barn"), LocalDate.now().minusYears(14)),
                    FORELDER,false,
                    Vedlegg(deler = listOf(UUID.randomUUID()))
                )
            ), Vedlegg(deler = listOf(
                UUID.randomUUID(),
                UUID.randomUUID())))
}