package no.nav.aap.api.søknad

import org.junit.jupiter.api.Test

//@SpringBootTest
//@ActiveProfiles(TEST)
//@ContextConfiguration(initializers = [MockOAuth2ServerInitializer::class])
internal class PDLIntegrationTest {


    @Test
    fun postnr() {
        println(PostnummerTjeneste().poststedFor("0360"))
    }

    @Test
    fun contextLoads() {
        val issuerId = "idporten"
    }
}