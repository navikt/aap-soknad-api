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
    fun listen(@Payload payload: JournalfoeringHendelseRecord)  {

        repo.getSøknadByEttersendingJournalpostid("${payload.journalpostId}")?.let { s ->
            log.trace("Søknad for ${payload.journalpostId} via ettersending er $s")
            s.ettersendinger.first {
                it.journalpostid == "${payload.journalpostId}"
            }.apply {
                journalpoststatus = payload.journalpostStatus
                journalfoert = payload.tilUTC()
            }
            return
        }
        repo.getSøknadByJournalpostid("${payload.journalpostId}")?.let {
            it.journalpoststatus = payload.journalpostStatus
            it.journalfoert = payload.tilUTC()
            log.trace("Søknad for ${payload.journalpostStatus}  er $it")
            return
        }
        log.trace("Ingen søknad/ettersending via for journalpost ${payload.journalpostId}/${payload.journalpostStatus} funnet i lokal DB ($payload)")
    }

    private fun JournalfoeringHendelseRecord.tilUTC()  = parse(hendelsesId.substringAfter('-')).toUTC()
}