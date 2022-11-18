package no.nav.aap.api.søknad.arkiv

import no.nav.aap.api.søknad.arkiv.ArkivConfig.Companion.JOARKHENDELSER
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@ConditionalOnGCP
class ArkivHendelseKonsument {
        private val log = getLogger(javaClass)

        @KafkaListener(topics = ["#{'\${joark.hendelser.topic}'}"], containerFactory = JOARKHENDELSER)
        @Transactional
        fun listen(@Payload hendelse: JournalfoeringHendelseRecord)  {
                log.info("Mottatt arkivføringshendelse med id ${hendelse.journalpostId} for  tema ${hendelse.behandlingstema}")
        }
}