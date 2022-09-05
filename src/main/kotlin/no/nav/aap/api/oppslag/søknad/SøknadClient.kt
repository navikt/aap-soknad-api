package no.nav.aap.api.oppslag.søknad

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.oppslag.søknad.SøknadClient.SøknadDTO.VedleggInfo
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.model.VedleggType
import no.nav.aap.util.AuthContext
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class SøknadClient(private val repo: SøknadRepository, private val ctx: AuthContext) {

    fun søknad(uuid: UUID) = søknad(ctx.getFnr(), uuid)

    fun søknad(fnr: Fødselsnummer, uuid: UUID) =
        repo.getSøknadByEventidAndFnr(uuid, fnr.fnr)?.let(::tilSøknad)

    fun søknader() = søknader(ctx.getFnr())
    fun søknader(fnr: Fødselsnummer) =
        repo.getSøknadByFnrOrderByCreatedDesc(fnr.fnr).map(::tilSøknad)

    private fun tilSøknad(s: Søknad) =
        with(s) {
            SøknadDTO(created,
                    eventid,
                    innsendtevedlegg.map { VedleggInfo(it.vedleggtype, it.created) },
                    manglendevedlegg.map { it.vedleggtype })
        }

    data class SøknadDTO(val innsendtDato: LocalDateTime?,
                         val søknadId: UUID,
                         val innsendteVedlegg: List<VedleggInfo>,
                         val manglendeVedlegg: List<VedleggType>) {
        data class VedleggInfo(val vedleggType: VedleggType, val innsendtDato: LocalDateTime?)
    }
}