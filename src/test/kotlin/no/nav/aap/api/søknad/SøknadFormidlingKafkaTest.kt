package no.nav.aap.api.søknad

import com.neovisionaries.i18n.CountryCode.AC
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.felles.Søker
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
import no.nav.aap.api.søknad.model.toKafkaObject
import no.nav.aap.util.Constants.TEST
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate.now
import java.util.concurrent.TimeUnit.MILLISECONDS


@SpringBootTest(classes = [KafkaAutoConfiguration::class, KafkaUtenlandsSøknadFormidler::class, KafkaTestSøknadKonsument::class])
@ActiveProfiles(TEST)
@DirtiesContext
@EmbeddedKafka(partitions = 2, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"])
@Disabled
internal class SøknadFormidlingKafkaTest {

    @Autowired
    private lateinit var formidler: UtenlandsSøknadFormidler

    @Autowired
    private lateinit var consumer: KafkaTestSøknadKonsument
    private val søknad = UtenlandsSøknadView(AC, Periode(now(), now().plusDays(20)))
    private val fnr = "01010111112"


    @Test
    fun fordelOgKonsumerSøknad() {
        formidler.sendUtenlandsSøknad(Fødselsnummer(fnr), søknad)
        consumer.latch.await(15000, MILLISECONDS)
        assertEquals(0, consumer.latch.count)
        assertEquals(søknad.toKafkaObject(Søker(Fødselsnummer(fnr), Navn("a", "b", "c"))), consumer.value)
    }
}