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
class KafkaTestSøknadConsumer {
    var value: UtenlandsSøknadKafka? = null
    val latch = CountDownLatch(1)

    @KafkaListener(topics = ["aap-utland-soknad-sendt.v1"],groupId ="test")
    fun receive(consumerRecord: ConsumerRecord<*, *>) {
        value = consumerRecord.value() as UtenlandsSøknadKafka
        latch.countDown()
    }
}