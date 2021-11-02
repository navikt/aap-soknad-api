package no.nav.aap.api.søknad

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.concurrent.CountDownLatch





@Profile("test")
@Component
class KafkaTestSøknadConsumer {
    private val log: Logger = LoggerFactory.getLogger(KafkaTestSøknadConsumer::class.java)
    var payload: String? = null
    val latch = CountDownLatch(1)


    @KafkaListener(topics = ["aap-utland-soknad-sendt.v1"])
    fun receive(consumerRecord: ConsumerRecord<*, *>) {
        log.info("received payload='{}'", consumerRecord.toString())
        payload = consumerRecord.toString()
        latch.countDown()
    }
}