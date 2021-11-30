package no.nav.aap.api.søknad

import no.nav.aap.util.Constants.TEST
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ActiveProfiles(TEST)
@ContextConfiguration(initializers = [MockOAuth2ServerInitializer::class])
internal class PDLIntegrationTest {


    @Test
    fun contextLoads() {
        val issuerId = "idporten"
    }
}