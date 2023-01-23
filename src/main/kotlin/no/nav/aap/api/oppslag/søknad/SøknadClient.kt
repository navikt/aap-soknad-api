package no.nav.aap.api.oppslag.søknad

import java.time.Instant
import java.time.ZoneOffset.UTC
import java.util.*
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagClient
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagMapper.DokumentOversiktInnslag
import no.nav.aap.api.saksbehandling.SaksbehandlingController.VedleggEtterspørsel
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Companion.SISTE_SØKNAD
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Ettersending
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.model.VedleggType
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SøknadClient(private val repo: SøknadRepository,
                   private val arkivClient: ArkivOppslagClient,
                   private val minside: MinSideClient,
                   private val ctx: AuthContext) {

    val log = getLogger(javaClass)

    @Transactional(readOnly = true)
    fun søknad(søknadId: UUID) = søknad(ctx.getFnr(), søknadId)

    fun søknad(fnr: Fødselsnummer, søknadId: UUID) =
        repo.getSøknadByEventidAndFnr(søknadId, fnr.fnr)?.let(::tilSøknad)

    @Transactional(readOnly = true)
    fun søknader(pageable: Pageable) = søknader(ctx.getFnr(), pageable)

    @Transactional
    fun etterspørrVedlegg(e: VedleggEtterspørsel) =
        repo.getSøknadByFnr(e.fnr.fnr,SISTE_SØKNAD).firstOrNull()?.let {
            log.trace("Oppretter oppgave for søknad ${it.eventid}")
            val oppgaveId = UUID.randomUUID()
            minside.opprettOppgave(e.fnr,it,"Eterspørr vedlegg",oppgaveId)
            log.trace("Opprettet oppgave med id $oppgaveId for søknad ${it.eventid}")
            it.registrerManglende(listOf(e.type),oppgaveId)
            oppgaveId
        }

    internal fun søknader(fnr: Fødselsnummer, pageable: Pageable) =
       repo.getSøknadByFnr(fnr.fnr, pageable).map(::tilSøknad)

    data class SøknadDTO(val innsendtDato: Instant?,
                         val søknadId: UUID,
                         val journalpostId: String,
                         val innsendteVedlegg: List<DokumentOversiktInnslag>,
                         val manglendeVedlegg: List<VedleggType>)

    private fun tilSøknad(s: Søknad) =
        with(s) {
            SøknadDTO(created?.toInstant(UTC),
                    eventid, journalpostid,
                    arkivClient.innsendteDokumenter(ettersendinger.map(Ettersending::eventid) + eventid), // TODO, for tung, slå opp alle først og plukk ut
                    manglendevedlegg.map { it.vedleggtype }).also {
                log.trace("Søknad er $it")
            }
        }
}