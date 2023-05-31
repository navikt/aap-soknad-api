package no.nav.aap.api.oppslag.arkiv

import io.micrometer.observation.annotation.Observed
import java.util.UUID
import org.springframework.stereotype.Component
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.størrelse

@Component
@Observed
class ArkivOppslagClient(private val adapter : ArkivOppslagWebClientAdapter) {

    private val log = getLogger(javaClass)

    fun dokument(journalpostId : String, dokumentId : String) =
        adapter.dokument(journalpostId, dokumentId)

    fun dokumenter() = adapter.dokumenter()

    fun søknadDokumentId(journalpostId : String) = adapter.søknadDokumentId(journalpostId)
        ?: throw IllegalStateException("Fant ikke søknadens dokumentId for $journalpostId")

    fun innsendteDokumenter(innsendingIds : List<UUID>) = dokumenter()
        .filter {
            it.innsendingId in innsendingIds.map(UUID::toString)
        }
        .also {
            log.trace("Slo opp {} fra {} ({})", it.størrelse("dokument"), innsendingIds, it)
        }
}