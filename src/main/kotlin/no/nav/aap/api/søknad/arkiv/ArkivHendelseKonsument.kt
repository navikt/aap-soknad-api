package no.nav.aap.api.søknad.arkiv

import java.time.LocalDateTime.parse
import no.nav.aap.api.søknad.arkiv.ArkivConfig.Companion.ARKIVHENDELSER
import no.nav.aap.api.søknad.fordeling.EttersendingRepository
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.TimeExtensions.toUTC
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.transaction.annotation.Transactional

@ConditionalOnGCP
class ArkivHendelseKonsument(private val repo: SøknadRepository, private val esRepo: EttersendingRepository) {
        private val log = getLogger(javaClass)

    @Transactional
    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = ARKIVHENDELSER)
    fun listen(@Payload hendelse: JournalfoeringHendelseRecord)  {
        repo.getSøknadByJournalpostid("${hendelse.journalpostId}")?.let {
            it.journalpoststatus = hendelse.journalpostStatus
            it.journalfoert = hendelse.tilUTC()
            log.info("Hendelse for journalpost ${hendelse.journalpostId} Type ${hendelse.hendelsesType}, Status ${hendelse.journalpostStatus} TEMA ${hendelse.temaNytt} håndtert")
        } ?: esRepo.getEttersendingByJournalpostid("${hendelse.journalpostId}")?.run {
            log.info("Søknadens ettersendinger er ${soknad?.ettersendinger}")
            soknad?.ettersendinger?.find { it.journalpostid == hendelse.journalpostStatus }?.let {
                    log.info("Journalpost ${hendelse.journalpostId} er for ettersending $it")
                    it.journalpoststatus = hendelse.journalpostStatus
                } ?: log.info("Fant ikke journalpost ${hendelse.journalpostId} i ${soknad?.ettersendinger}")
        } ?: log.info("Ingen søknad eller ettersendelse for journalpost ${hendelse.journalpostId} funnet for hendelse $hendelse")
    }

    private fun JournalfoeringHendelseRecord.tilUTC()  = parse(hendelsesId.substringAfter('-')).toUTC()
}