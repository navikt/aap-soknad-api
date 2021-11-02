package no.nav.aap.api.søknad

import com.neovisionaries.i18n.CountryCode.AC
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.oppslag.pdl.PDLWebClientAdapter
import no.nav.aap.api.rest.tokenx.TokenXFilterFunction
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate.now
import java.util.concurrent.TimeUnit


@SpringBootTest
@EnableMockOAuth2Server
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = arrayOf("listeners=PLAINTEXT://localhost:9092","port=9092"))
internal class  AAPAPIApplicationTests {

    @Autowired
    private val formidler: SøknadFormidler? = null
    @Autowired
    private lateinit var consumer: KafkaTestSøknadConsumer


    @MockBean
    private lateinit var pdl: PDLWebClientAdapter
    @MockBean
    private lateinit var tokenX: TokenXFilterFunction

    @Test
    fun contextLoads() {
        formidler!!.sendUtlandsSøknad(Fødselsnummer("01010111111"), UtenlandsSøknadView(AC, Periode(now(), now().plusDays(20))))
        consumer.latch.await(10000, TimeUnit.MILLISECONDS);
        //assertEquals(0,consumer.latch.count)
    }
}