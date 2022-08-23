package no.nav.aap.api.søknad.ettersendelse

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.model.VedleggType
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class EttersendelseClient(private val repo: SøknadRepository) {
    fun søknaderMedMangler(fnr: Fødselsnummer) =
        repo.getSøknadByFnr(fnr.fnr)?.filter { s ->
            s.manglendevedlegg.isNotEmpty()
        }?.map(::tilSøknad)

    private fun tilSøknad(s: Søknad) =
        with(s) {
            SøknadDTO(Fødselsnummer(fnr), journalpostid, created, manglendevedlegg.map { it.vedleggtype }.toSet())
        }

    data class SøknadDTO(val fnr: Fødselsnummer,
                         val journalpostId: String,
                         val opprettet: LocalDateTime?,
                         val mangler: Set<VedleggType>)
}