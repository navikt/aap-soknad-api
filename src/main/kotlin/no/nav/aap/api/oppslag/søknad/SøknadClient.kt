package no.nav.aap.api.oppslag.søknad

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.model.VedleggType
import no.nav.aap.util.AuthContext
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class SøknadClient(private val repo: SøknadRepository, private val ctx: AuthContext) {

    fun søknader() = søknader(ctx.getFnr())
    fun søknader(fnr: Fødselsnummer) =
        repo.getSøknadByFnrOrderByCreatedDesc(fnr.fnr)?.map(::tilSøknad)

    private fun tilSøknad(s: Søknad) =
        with(s) {
            SøknadDTO(created,
                    eventid,
                    innsendtevedlegg.map { it.vedleggtype },
                    manglendevedlegg.map { it.vedleggtype })
        }

    data class SøknadDTO(val opprettet: LocalDateTime?,
                         val søknadId: UUID,
                         val innsendte: List<VedleggType>,
                         val mangler: List<VedleggType>)
}