package no.nav.aap.api.søknad.arkiv

import no.nav.aap.api.søknad.arkiv.ArkivConfig.Companion.ARKIVHENDELSER
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
@ConditionalOnGCP
class ArkivHendelseKonsument {
        private val log = getLogger(javaClass)

        @KafkaListener(topics = ["#{'\${joark.hendelser.topic}'}"], groupId = "#{'\${joark.hendelser.groupid}'}", containerFactory = ARKIVHENDELSER)
        fun listen(@Payload hendelse: JournalfoeringHendelseRecord)  {
                log.info("Mottatt arkivføringshendelse $hendelse")
        }
}