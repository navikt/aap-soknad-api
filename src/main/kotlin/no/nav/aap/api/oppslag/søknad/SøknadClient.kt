package no.nav.aap.api.oppslag.søknad

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.util.*
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagClient
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagMapper.DokumentOversiktInnslag
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Ettersending
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.model.VedleggType
import no.nav.aap.util.AuthContext
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SøknadClient(private val repo: SøknadRepository,
                   private val arkivClient: ArkivOppslagClient,
                   private val ctx: AuthContext) {

    fun søknad(søknadId: UUID) = søknad(ctx.getFnr(), søknadId)

    fun søknad(fnr: Fødselsnummer, søknadId: UUID) =
        repo.getSøknadByEventidAndFnr(søknadId, fnr.fnr)?.let(::tilSøknad)

    fun søknader(pageable: Pageable) = søknader(ctx.getFnr(), pageable)
    internal fun søknader(fnr: Fødselsnummer, pageable: Pageable) =
        repo.getSøknadByFnr(fnr.fnr, pageable).map(::tilSøknad)

    data class SøknadDTO(val innsendtDato: LocalDateTime?,
                         val søknadId: UUID,
                         val journalpostId: String,
                         val innsendteVedlegg: List<DokumentOversiktInnslag>,
                         val manglendeVedlegg: List<VedleggType>)

    private fun tilSøknad(s: Søknad) =
        with(s) {
            SøknadDTO(created,
                    eventid, journalpostid,
                    arkivClient.innsendteDokumenter(ettersendinger.map(Ettersending::eventid) + eventid), // TODO, for tung, slå opp alle først og plukk ut
                    manglendevedlegg.map { it.vedleggtype })
        }
}