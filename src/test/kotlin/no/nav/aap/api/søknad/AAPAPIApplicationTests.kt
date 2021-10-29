package no.nav.aap.api.søknad

import no.nav.aap.api.oppslag.pdl.PDLWebClientAdapter
import no.nav.aap.api.rest.tokenx.TokenXFilterFunction
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@EnableMockOAuth2Server
@ActiveProfiles("test")
internal class  AAPAPIApplicationTests {

    @MockBean
    private lateinit var pdl: PDLWebClientAdapter
    @MockBean
    private lateinit var tokenX: TokenXFilterFunction

    @Test
    fun contextLoads() {
        val issuerId = "default"
    }
    companion object {

    }
}