package no.nav.aap.api

import java.time.LocalDate.now
import no.nav.aap.api.oppslag.person.PDLAdresseBeskyttelse.FORTROLIG
import no.nav.aap.api.oppslag.person.PDLAdresseBeskyttelse.STRENGT_FORTROLIG
import no.nav.aap.api.oppslag.person.PDLAdresseBeskyttelse.STRENGT_FORTROLIG_UTLAND
import no.nav.aap.api.oppslag.person.PDLBolkBarn.PDLBarn
import no.nav.aap.api.oppslag.person.PDLBolkBarn.PDLBarn.PDLDødsfall
import no.nav.aap.api.oppslag.person.PDLGradering
import no.nav.aap.api.oppslag.person.PDLNavn
import no.nav.aap.api.oppslag.person.PDLSøker.PDLFødsel

object OMBarn {
    fun listeMedPDLBarn() = sequenceOf(
        PDLBarn(
            setOf(PDLFødsel(now())),
            setOf(PDLNavn("Barn", "B", "Barnsben")),
            setOf(PDLGradering(STRENGT_FORTROLIG)),
            setOf(PDLDødsfall(now()))
        ),
        PDLBarn(
            setOf(PDLFødsel(now())),
            setOf(PDLNavn("Barn", "B", "Barnsben")),
            setOf(PDLGradering(FORTROLIG)),
            setOf(PDLDødsfall(now()))
        ),
        PDLBarn(
            setOf(PDLFødsel(now())),
            setOf(PDLNavn("Barn", "B", "Barnsben")),
            setOf(PDLGradering(STRENGT_FORTROLIG_UTLAND)),
            setOf(PDLDødsfall(now()))
        ),
        PDLBarn(
            setOf(PDLFødsel(now())),
            setOf(PDLNavn("Barn", "B", "Barnsben")),
            null,
            setOf(PDLDødsfall(now()))
        ),
        PDLBarn(
            setOf(PDLFødsel(now())),
            setOf(PDLNavn("Barn", "B", "Barnsben")),
            null,null
        ))
}