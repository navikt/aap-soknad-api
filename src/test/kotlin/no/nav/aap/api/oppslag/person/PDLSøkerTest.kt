package no.nav.aap.api.oppslag.person

import kotlin.test.BeforeTest
import kotlin.test.assertTrue
import no.nav.aap.api.OMBarn
import no.nav.aap.api.OMBarn.listeMedPDLBarn
import no.nav.aap.api.OMPersoner.har_barn
import no.nav.aap.api.oppslag.person.PDLMapper.pdlBarnTilBarn
import org.junit.Test

class PDLSøkerTest {

    @BeforeTest
    fun sjekk_person_med_barn_for_visning() {
        assertTrue(har_barn().barn.isNotEmpty())
    }

    @BeforeTest
    fun sjekk_liste_barn() {
        assertTrue { !listeMedPDLBarn().none() }
    }

    @Test
    fun pdlbarn_til_barn() {
        assertTrue { pdlBarnTilBarn(OMBarn.listeMedPDLBarn()).size == 1 }
    }
}