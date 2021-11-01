package no.nav.aap.api.søknad

import no.nav.aap.api.oppslag.pdl.PDLWebClientAdapter
import no.nav.aap.api.rest.tokenx.TokenXFilterFunction
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles


@SpringBootTest
@EnableMockOAuth2Server
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = arrayOf("listeners=PLAINTEXT://localhost:9092","port=9092"))
internal class  AAPAPIApplicationTests {

    @Autowired
    private val consumer: KafkaConsumer<*, *>? = null

    @Autowired
    private val producer: KafkaProducer<*, *>? = null


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