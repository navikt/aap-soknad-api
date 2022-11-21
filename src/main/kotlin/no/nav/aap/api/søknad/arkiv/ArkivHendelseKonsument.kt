package no.nav.aap.api.søknad.arkiv

import java.time.LocalDateTime
import java.time.LocalDateTime.parse
import java.time.ZoneId.of
import java.time.ZoneId.systemDefault
import java.time.ZoneOffset.UTC
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
                log.info("Mottatt arkivføringshendelse $hendelse ${systemDefault()}")
                repo.getSøknadByJournalpostid("${hendelse.journalpostId}")?.let {
                        log.info("Fant opprinnelig søknad $it for arkivføringshendelse $hendelse")
                        it.journalfoert = hendelse.tilTid()
                }
        }

    private fun JournalfoeringHendelseRecord.tilTid()  = parse(hendelsesId.substringAfter('-')).toUTC()
    private fun LocalDateTime.toUTC(): LocalDateTime = atZone(of("Europe/Oslo")).withZoneSameInstant(UTC).toLocalDateTime()
}