package no.nav.aap.api.oppslag.pdl

import no.nav.aap.api.OMBarn
import no.nav.aap.api.OMPersoner
import org.junit.Before
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertTrue

class PDLSøkerTest {

    @BeforeTest
    fun sjekk_person_med_barn_for_visning() {
        val person = OMPersoner.har_barn()
        assertTrue ( !person.barn.isEmpty() )
    }

    @BeforeTest
    fun sjekk_liste_barn() {
        val barnList = OMBarn.listeMedPDLBarn()
        assertTrue { !barnList.none() }
    }

    @Test
    fun pdlbarn_til_barn() {
        val pdlbarnList = OMBarn.listeMedPDLBarn()
        val barnList = PDLMapper.pdlBarnTilBarn(pdlbarnList)
        assertTrue { barnList.size == 1 }
    }
}