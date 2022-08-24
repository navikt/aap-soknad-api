package no.nav.aap.api.søknad.minside

import no.nav.aap.api.søknad.minside.MinSideBeskjedRepository.Beskjed
import no.nav.aap.api.søknad.minside.MinSideOppgaveRepository.Oppgave
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.doknotifikasjon.schemas.DoknotifikasjonStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class EksternNotifikasjonStatusKonsument(private val repos: MinSideRepositories) {
    private val log = getLogger(javaClass)

    @KafkaListener(topics = ["teamdokumenthandtering.aapen-dok-notifikasjon-status"],
            containerFactory = "notifikasjonListenerContainerFactory")
    @Transactional
    fun listen(@Payload status: DoknotifikasjonStatus) = oppdaterDistribusjonStatus(status)

    private fun oppdaterDistribusjonStatus(status: DoknotifikasjonStatus) {
        with(status.eventId()) {
            repos.oppgaver.findOppgaveByEventid(this)?.let {
                oppdaterOppgave(it, status)
            } ?: repos.beskjeder.findBeskjedByEventid(this)?.let {
                oppdaterBeskjed(it, status)
            } ?: log.warn("Fant ingen beskjed/oppgave med eventid $this i DB (dette skal aldri skje)")
        }
    }

    private fun oppdaterOppgave(oppgave: Oppgave, status: DoknotifikasjonStatus) =
        with(status) {
            log.trace("Oppdaterer oppgave med distribusjonsinfo fra $status")
            oppgave.notifikasjoner.add(EksternOppgaveNotifikasjon(
                    oppgave = oppgave,
                    eventid = eventId(),
                    distribusjonid = distribusjonId,
                    distribusjonkanal = melding))
            repos.oppgaver.save(oppgave).also {
                log.trace("Oppdatert oppgave $it med distribusjonsinfo fra $this i DB")
            }
        }

    private fun oppdaterBeskjed(beskjed: Beskjed, status: DoknotifikasjonStatus) =
        with(status) {
            log.trace("Oppdaterer beskjed med distribusjonsinfo fra $status")
            beskjed.notifikasjoner.add(EksternBeskjedNotifikasjon(
                    beskjed = beskjed,
                    eventid = eventId(),
                    distribusjonid = distribusjonId,
                    distribusjonkanal = melding))
            repos.beskjeder.save(beskjed).also {
                log.trace("Oppdatert beskjed $it med distribusjonsinfo fra $this i DB")
            }
        }

    private fun DoknotifikasjonStatus.eventId() = UUID.fromString(bestillingsId)

    companion object {
        const val FERDIGSTILT = "FERDIGSTILT"
        const val FEILET = "FEILET"
        const val NOTIFIKASJON_SENDT = "notifikasjon sendt via"
    }
}