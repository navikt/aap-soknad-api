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
        with(payload) {
            repo.getSøknadByEttersendingJournalpostid("$journalpostId")?.let { s ->
                log.trace("Søknad for $journalpostId via ettersending er $s")
                s.ettersendinger.first {
                    it.journalpostid == "$journalpostId"
                }.apply {
                    journalpoststatus = journalpostStatus
                    journalfoert = tilUTC()
                }
                return
            }
            repo.getSøknadByJournalpostid("$journalpostId")?.let {
                it.journalpoststatus = journalpostStatus
                it.journalfoert = tilUTC()
                log.trace("Søknad for $journalpostStatus  er $it")
                return
            }
            log.trace("Ingen søknad/ettersending via for journalpost $journalpostId/$journalpostStatus funnet i lokal DB")
        }
    }

    private fun JournalfoeringHendelseRecord.tilUTC()  = parse(hendelsesId.substringAfter('-')).toUTC()
}