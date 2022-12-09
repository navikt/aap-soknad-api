package no.nav.aap.api

import java.time.LocalDate
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.oppslag.pdl.PDLAdresseBeskyttelse
import no.nav.aap.api.oppslag.pdl.PDLBarn
import no.nav.aap.api.oppslag.pdl.PDLGradering
import no.nav.aap.api.oppslag.pdl.PDLNavn
import no.nav.aap.api.oppslag.pdl.PDLSøker
import no.nav.aap.api.søknad.model.Søker.Barn

object OMBarn {
    fun enkeltBarn() = Barn(Navn("Barn", "B", "Barnsben"), LocalDate.now())

    fun listeMedPDLBarn() = sequenceOf(
        PDLBarn(
            setOf(PDLSøker.PDLFødsel(LocalDate.now())),
            setOf(PDLNavn("Barn", "B", "Barnsben")),
            setOf(PDLGradering(PDLAdresseBeskyttelse.STRENGT_FORTROLIG)),
            setOf(PDLBarn.PDLDødsfall(LocalDate.now()))
        ),
        PDLBarn(
            setOf(PDLSøker.PDLFødsel(LocalDate.now())),
            setOf(PDLNavn("Barn", "B", "Barnsben")),
            setOf(PDLGradering(PDLAdresseBeskyttelse.FORTROLIG)),
            setOf(PDLBarn.PDLDødsfall(LocalDate.now()))
        ),
        PDLBarn(
            setOf(PDLSøker.PDLFødsel(LocalDate.now())),
            setOf(PDLNavn("Barn", "B", "Barnsben")),
            setOf(PDLGradering(PDLAdresseBeskyttelse.STRENGT_FORTROLIG_UTLAND)),
            setOf(PDLBarn.PDLDødsfall(LocalDate.now()))
        ),
        PDLBarn(
            setOf(PDLSøker.PDLFødsel(LocalDate.now())),
            setOf(PDLNavn("Barn", "B", "Barnsben")),
            null,
            setOf(PDLBarn.PDLDødsfall(LocalDate.now()))
        ),
        PDLBarn(
            setOf(PDLSøker.PDLFødsel(LocalDate.now())),
            setOf(PDLNavn("Barn", "B", "Barnsben")),
            null,null
        ))
}