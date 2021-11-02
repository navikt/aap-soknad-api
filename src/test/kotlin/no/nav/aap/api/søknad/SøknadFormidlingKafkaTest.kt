package no.nav.aap.api.søknad

import com.neovisionaries.i18n.CountryCode.AC
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
import no.nav.aap.api.søknad.model.toKafkaObject
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate.now
import java.util.concurrent.TimeUnit.MILLISECONDS


@SpringBootTest(classes= [KafkaAutoConfiguration::class, KafkaSøknadFormidler::class, KafkaTestSøknadConsumer::class])
@EnableMockOAuth2Server
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"])
internal class  SøknadFormidlingKafkaTest {

    @Autowired
    private lateinit var formidler: SøknadFormidler
    @Autowired
    private lateinit var consumer: KafkaTestSøknadConsumer
    private val utenlandsSøknadView = UtenlandsSøknadView(AC, Periode(now(), now().plusDays(20)))
    private val fnr = "01010111111"

    @Test
    fun contextLoads() {
        formidler.sendUtlandsSøknad(Fødselsnummer(fnr), utenlandsSøknadView)
        consumer.latch.await(10000, MILLISECONDS);
        assertEquals(0,consumer.latch.count)
        assertEquals(utenlandsSøknadView.toKafkaObject(fnr),consumer.value)
    }
}