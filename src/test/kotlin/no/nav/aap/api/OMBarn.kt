package no.nav.aap.api

import no.nav.aap.api.felles.Navn
import no.nav.aap.api.oppslag.pdl.*
import no.nav.aap.api.søknad.model.Søker.Barn
import java.time.LocalDate

object OMBarn {
    fun barn(): Barn {
        return Barn(Navn("Barn", "B", "Barnsben"), LocalDate.now())
    }

    fun listeMedPDLBarn(): List<PDLBarn>{
        return listOf(
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
            )
        )
    }

}