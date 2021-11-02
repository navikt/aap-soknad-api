package no.nav.aap.api.søknad

import no.nav.aap.api.config.Constants.TEST
import no.nav.aap.api.søknad.model.UtenlandsSøknadKafka
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.concurrent.CountDownLatch

@Profile(TEST)
@Component
class KafkaTestSøknadKonsument {
    var value: UtenlandsSøknadKafka? = null
    val latch = CountDownLatch(1)

    @KafkaListener(topics = ["#{'\${utenlands.topic}'}"])
    fun konsumer(consumerRecord: ConsumerRecord<String, UtenlandsSøknadKafka>) {
        value = consumerRecord.value()
        latch.countDown()
    }
}