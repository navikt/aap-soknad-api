package no.nav.aap.api.søknad

import no.nav.aap.api.søknad.model.UtenlandsSøknadKafka
import no.nav.aap.api.util.LoggerUtil
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import java.util.concurrent.CountDownLatch

class KafkaTestSøknadKonsument {
    private val log = LoggerUtil.getLogger(javaClass)
    var value: UtenlandsSøknadKafka? = null
    val latch = CountDownLatch(1)

    @KafkaListener(topics = ["#{'\${utenlands.topic:aap.aap-utland-soknad-sendt.v1}'}"], groupId = "test")
    fun konsumer(consumerRecord: ConsumerRecord<String, UtenlandsSøknadKafka>) {
        log.info("Konsumert ${consumerRecord.key()}  fra ${consumerRecord.offset()}")
        value = consumerRecord.value()
        latch.countDown()
    }
}