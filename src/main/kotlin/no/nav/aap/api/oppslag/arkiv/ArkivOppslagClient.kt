package no.nav.aap.api.oppslag.arkiv

import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagJournalpostType.I
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.størrelse
import org.springframework.stereotype.Component
import java.util.*

@Component
class ArkivOppslagClient(private val adapter: ArkivOppslagWebClientAdapter) {
    private val log = getLogger(javaClass)

    fun dokument(journalpostId: String, dokumentId: String) =
        adapter.dokument(journalpostId, dokumentId)

    fun dokumenter() = adapter.dokumenter()
    fun innsendteDokumenter(innsendingIds: List<UUID>) = dokumenter()
        .filter {
            it.innsendingId in innsendingIds }
        .also {
            log.trace("Slo opp ${it.størrelse("dokument")} fra $innsendingIds ($it)")
    }
}