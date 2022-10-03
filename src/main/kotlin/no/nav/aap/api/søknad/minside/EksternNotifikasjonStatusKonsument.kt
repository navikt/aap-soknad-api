package no.nav.aap.api.søknad.minside

import no.nav.aap.api.søknad.minside.MinSideBeskjedRepository.Beskjed
import no.nav.aap.api.søknad.minside.MinSideBeskjedRepository.EksternBeskjedNotifikasjon
import no.nav.aap.api.søknad.minside.MinSideOppgaveRepository.EksternOppgaveNotifikasjon
import no.nav.aap.api.søknad.minside.MinSideOppgaveRepository.Oppgave
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.doknotifikasjon.schemas.DoknotifikasjonStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class EksternNotifikasjonStatusKonsument(private val repos: MinSideRepositories) {
    private val log = getLogger(javaClass)

    @KafkaListener(topics = ["teamdokumenthandtering.aapen-dok-notifikasjon-status"],
            containerFactory = DOKNOTIFIKASJON)
    @Transactional
    fun listen(@Payload status: DoknotifikasjonStatus) = oppdaterDistribusjonStatus(status)

    private fun oppdaterDistribusjonStatus(status: DoknotifikasjonStatus) {
        with(status.eventId()) {
            repos.oppgaver.findByEventid(this)?.let {
                oppdaterOppgave(it, status)
            } ?: repos.beskjeder.findByEventid(this)?.let {
                oppdaterBeskjed(it, status)
            } ?: log.warn("Fant ingen beskjed/oppgave med eventid $this i DB (dette skal aldri skje)")
        }
    }

    private fun oppdaterOppgave(oppgave: Oppgave, status: DoknotifikasjonStatus) =
        with(status) {
            log.trace("Oppdaterer oppgave med distribusjonsinfo fra $status")
            oppgave.notifikasjoner.add(EksternOppgaveNotifikasjon(oppgave, eventId(), distribusjonId, melding))
            repos.oppgaver.save(oppgave).also {
                log.trace("Oppdatert oppgave $it med distribusjonsinfo fra $this i DB")
            }
        }

    private fun oppdaterBeskjed(beskjed: Beskjed, status: DoknotifikasjonStatus) =
        with(status) {
            log.trace("Oppdaterer beskjed med distribusjonsinfo fra $status")
            beskjed.notifikasjoner.add(EksternBeskjedNotifikasjon(beskjed, eventId(), distribusjonId, melding))
            repos.beskjeder.save(beskjed).also {
                log.trace("Oppdatert beskjed $it med distribusjonsinfo fra $this i DB")
            }
        }

    private fun DoknotifikasjonStatus.eventId() = UUID.fromString(bestillingsId)

    companion object {
        const val DOKNOTIFIKASJON = "doknotifikasjon"
        const val FERDIGSTILT = "FERDIGSTILT"
        const val FEILET = "FEILET"
        const val NOTIFIKASJON_SENDT = "notifikasjon sendt via"
    }
}