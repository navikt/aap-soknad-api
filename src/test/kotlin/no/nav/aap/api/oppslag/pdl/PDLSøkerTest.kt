package no.nav.aap.api.oppslag.pdl

import no.nav.aap.api.OMPersoner
import org.junit.Test
import kotlin.test.assertTrue

class PDLSøkerTest {

    @Test
    fun sjekk_person_med_barn_for_visning() {
        val person = OMPersoner.har_barn()
        assertTrue ( !person.barn.isEmpty() )
    }
}