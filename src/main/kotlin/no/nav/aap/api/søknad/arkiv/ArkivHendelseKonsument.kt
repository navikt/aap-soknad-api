package no.nav.aap.api.søknad.arkiv

import no.nav.aap.api.søknad.arkiv.ArkivConfig.Companion.ARKIVHENDELSER
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.transaction.annotation.Transactional

@ConditionalOnGCP
class ArkivHendelseKonsument(private val repo: SøknadRepository) {
        private val log = getLogger(javaClass)

      @Transactional
        @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = ARKIVHENDELSER)
        fun listen(@Payload hendelse: JournalfoeringHendelseRecord)  {
                log.info("Mottatt en arkivføringshendelse $hendelse")
                repo.getSøknadByJournalpostid("${hendelse.journalpostId}")?.let {
                        log.info("Fant opprinnelig søknad fra arkivføringshendelse $it")
                } ?:  log.info("Fant ikke opprinnelig søknad fra arkivføringshendelse for ${hendelse.journalpostId}")
        }
}