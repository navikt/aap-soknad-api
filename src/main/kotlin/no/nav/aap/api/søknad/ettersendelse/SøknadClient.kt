package no.nav.aap.api.søknad.ettersendelse

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.model.VedleggType
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class SøknadClient(private val repo: SøknadRepository) {
    fun søknader(fnr: Fødselsnummer) =
        repo.getSøknadByFnrOrderByCreatedDesc(fnr.fnr)?.map(::tilSøknad)

    private fun tilSøknad(s: Søknad) =
        with(s) {
            SøknadDTO(created,
                    eventid,
                    innsendtevedlegg.map { it.vedleggtype }.toSet(),
                    manglendevedlegg.map { it.vedleggtype }.toSet())
        }

    data class SøknadDTO(val opprettet: LocalDateTime?,
                         val søknadId: UUID,
                         val innsendte: Set<VedleggType>,
                         val mangler: Set<VedleggType>)
}