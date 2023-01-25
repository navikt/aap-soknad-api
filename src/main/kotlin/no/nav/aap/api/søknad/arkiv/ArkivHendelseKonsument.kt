package no.nav.aap.api.søknad.arkiv

import java.time.LocalDateTime.parse
import no.nav.aap.api.søknad.arkiv.ArkivConfig.Companion.ARKIVHENDELSER
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.TimeExtensions.toUTC
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

        repo.getSøknadByEttersendingJournalpostid("${hendelse.journalpostId}")?.let { s ->
            log.info("Søknad ettersendinger via ettersending journalpost ${hendelse.journalpostId}  er ${s.ettersendinger}")
            s.ettersendinger.first {
                it.journalpostid == "${hendelse.journalpostId}"
            }.journalpoststatus = hendelse.journalpostStatus
            log.info("Søknad ettersendinger via ettersending etter status journalpost ${hendelse.journalpostId}  er ${s.ettersendinger}")
            return
        }

        repo.getSøknadByJournalpostid("${hendelse.journalpostId}")?.let {
            it.journalpoststatus = hendelse.journalpostStatus
            it.journalfoert = hendelse.tilUTC()
            log.info("Søknad direkte ingen via for ${hendelse.journalpostStatus} er $it")
            return
        }
        log.info("Ingen søknad/ettersending for journalpost ${hendelse.journalpostId} funnet i lokal DB")
    }

    private fun JournalfoeringHendelseRecord.tilUTC()  = parse(hendelsesId.substringAfter('-')).toUTC()
}