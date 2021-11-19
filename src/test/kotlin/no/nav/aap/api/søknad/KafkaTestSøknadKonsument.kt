package no.nav.aap.api.søknad

import no.nav.aap.api.søknad.model.UtenlandsSøknadKafka
import no.nav.aap.api.util.LoggerUtil
import no.nav.aap.api.util.MDCUtil
import no.nav.aap.api.util.MDCUtil.NAV_CALL_ID
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import java.util.concurrent.CountDownLatch

class KafkaTestSøknadKonsument {
    private val log = LoggerUtil.getLogger(javaClass)
    var value: UtenlandsSøknadKafka? = null
    val latch = CountDownLatch(1)

    @KafkaListener(topics = ["#{'\${utenlands.topic:aap.aap-utland-soknad-sendt.v1}'}"], groupId = "test")
    fun konsumer(consumerRecord: ConsumerRecord<String, UtenlandsSøknadKafka>, @Header(NAV_CALL_ID)  callId: String) {
        MDCUtil.toMDC(NAV_CALL_ID,callId)
        log.info("Konsumert ${consumerRecord.key()}  fra ${consumerRecord.offset()}")
        value = consumerRecord.value()
        latch.countDown()
    }
}