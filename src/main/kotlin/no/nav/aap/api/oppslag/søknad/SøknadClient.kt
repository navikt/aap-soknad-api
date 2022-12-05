package no.nav.aap.api.oppslag.søknad

import java.time.Instant
import java.time.ZoneOffset
import java.util.*
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagClient
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagMapper.DokumentOversiktInnslag
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Ettersending
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.model.VedleggType
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SøknadClient(private val repo: SøknadRepository,
                   private val arkivClient: ArkivOppslagClient,
                   private val ctx: AuthContext) {

    val log = getLogger(javaClass)

    @Transactional
    fun søknad(søknadId: UUID) = søknad(ctx.getFnr(), søknadId)

    fun søknad(fnr: Fødselsnummer, søknadId: UUID) =
        repo.getSøknadByEventidAndFnr(søknadId, fnr.fnr)?.let(::tilSøknad)

    @Transactional
    fun søknader(pageable: Pageable) = søknader(ctx.getFnr(), pageable)
    internal fun søknader(fnr: Fødselsnummer, pageable: Pageable) =
        repo.getSøknadByFnr(fnr.fnr, pageable).map(::tilSøknad)

    data class SøknadDTO(val innsendtDato: Instant?,
                         val søknadId: UUID,
                         val journalpostId: String,
                         val innsendteVedlegg: List<DokumentOversiktInnslag>,
                         val manglendeVedlegg: List<VedleggType>)

    private fun tilSøknad(s: Søknad) =
        with(s) {
            SøknadDTO(created?.toInstant(ZoneOffset.UTC),
                    eventid, journalpostid,
                    arkivClient.innsendteDokumenter(ettersendinger.map(Ettersending::eventid) + eventid), // TODO, for tung, slå opp alle først og plukk ut
                    manglendevedlegg.map { it.vedleggtype }).also {
                log.trace("Søknad er $it")
            }
        }
}