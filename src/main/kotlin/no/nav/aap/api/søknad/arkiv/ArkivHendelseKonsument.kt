package no.nav.aap.api.søknad.arkiv

import no.nav.aap.api.søknad.arkiv.ArkivConfig.Companion.ARKIVHENDELSER
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.TimeExtensions.toUTC
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime.parse

@ConditionalOnGCP
class ArkivHendelseKonsument(private val repo: SøknadRepository) {
    private val log = getLogger(javaClass)

    @Transactional
    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = ARKIVHENDELSER)
    fun listen(@Payload payload: JournalfoeringHendelseRecord)  =
        oppdaterVedlegg(payload) ?: oppdaterSøknad(payload)
        ?: log.trace("Ingen søknad/ettersending via for journalpost ${payload.journalpostId}/${payload.journalpostStatus} funnet i lokal DB")

    private fun oppdaterVedlegg(payload: JournalfoeringHendelseRecord) =
        with(payload) {
            repo.getSøknadByEttersendingJournalpostid("$journalpostId")?.let { søknad ->
                søknad.ettersendinger.first {
                    it.journalpostid == "$journalpostId"
                }.apply {
                    journalpoststatus = journalpostStatus
                    journalfoert = tilUTC()
                }
            }
        }

    private fun oppdaterSøknad(payload: JournalfoeringHendelseRecord) =
        with(payload) {
            repo.getSøknadByJournalpostid("$journalpostId")?.let {søknad ->
                log.info("Oppdaterer søknad med id $journalpostId med status $journalpostStatus ")
                søknad.apply {
                    journalpoststatus = journalpostStatus
                    journalfoert = tilUTC()
                }
            }
        }


    private fun JournalfoeringHendelseRecord.tilUTC()  = parse(hendelsesId.substringAfter('-')).toUTC()
}