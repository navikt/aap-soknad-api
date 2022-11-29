package no.nav.aap.api

import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.*
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler
import no.nav.aap.api.søknad.model.*
import java.time.LocalDate
import java.util.*

object OMSøknad {

        fun standard_soknad(): StandardSøknad {
            return StandardSøknad(
                false,
                null,
                Studier(Studier.StudieSvar.NEI, RadioValg.NEI),
                Medlemskap(true, null, null, null,
                    listOf(
                        Utenlandsopphold(
                            CountryCode.SE,
                        Periode(LocalDate.now(), LocalDate.now().plusDays(2)),
                        true, "11111111")
                    )),
                listOf(
                    RegistrertBehandler(
                        RegistrertBehandler.BehandlerType.FASTLEGE, RegistrertBehandler.BehandlerKategori.LEGE, Navn("Lege", "A", "Legesen"),
                    RegistrertBehandler.KontaktInformasjon("Legekontoret",
                        OrgNummer("888888888"),
                        Adresse("Legegata", "17", "A",
                            PostNummer("2600", "Lillehammer")
                        ),
                        "22222222")
                    )
                ), emptyList(),
                StandardSøknad.Yrkesskade.JA,
                Utbetalinger(
                    Utbetalinger.FraArbeidsgiver(true, Vedlegg(deler = listOf(
                        UUID.randomUUID(),
                        UUID.randomUUID()))
                    ), listOf(Utbetalinger.AnnenStønad(Utbetalinger.AnnenStønadstype.INTRODUKSJONSSTØNAD))),
                "Tillegg",
                listOf(BarnOgInntekt(true)),
                listOf(
                    AnnetBarnOgInntekt(
                        Søker.Barn(Navn("Et", "ekstra", "Barn"), LocalDate.now().minusYears(14)),
                        AnnetBarnOgInntekt.Relasjon.FORELDER,false,
                        Vedlegg(deler = listOf(UUID.randomUUID()))
                    )
                ), Vedlegg(deler = listOf(
                    UUID.randomUUID(),
                    UUID.randomUUID()))
            )
        }

        fun er_student_søknad(): StandardSøknad{
            return StandardSøknad(
                false,
                null,
                Studier(Studier.StudieSvar.AVBRUTT, RadioValg.JA),
                Medlemskap(true, null, null, null,
                    listOf(
                        Utenlandsopphold(
                            CountryCode.SE,
                            Periode(LocalDate.now(), LocalDate.now().plusDays(2)),
                            true, "11111111")
                    )),
                listOf(
                    RegistrertBehandler(
                        RegistrertBehandler.BehandlerType.FASTLEGE, RegistrertBehandler.BehandlerKategori.LEGE, Navn("Lege", "A", "Legesen"),
                        RegistrertBehandler.KontaktInformasjon("Legekontoret",
                            OrgNummer("888888888"),
                            Adresse("Legegata", "17", "A",
                                PostNummer("2600", "Lillehammer")
                            ),
                            "22222222")
                    )
                ), emptyList(),
                StandardSøknad.Yrkesskade.JA,
                Utbetalinger(
                    Utbetalinger.FraArbeidsgiver(true, Vedlegg(deler = listOf(
                        UUID.randomUUID(),
                        UUID.randomUUID()))
                    ), listOf(Utbetalinger.AnnenStønad(Utbetalinger.AnnenStønadstype.INTRODUKSJONSSTØNAD))),
                "Tillegg",
                listOf(BarnOgInntekt(true)),
                listOf(
                    AnnetBarnOgInntekt(
                        Søker.Barn(Navn("Et", "ekstra", "Barn"), LocalDate.now().minusYears(14)),
                        AnnetBarnOgInntekt.Relasjon.FORELDER,false,
                        Vedlegg(deler = listOf(UUID.randomUUID()))
                    )
                ), Vedlegg(deler = listOf(
                    UUID.randomUUID(),
                    UUID.randomUUID()))
            )
        }
}