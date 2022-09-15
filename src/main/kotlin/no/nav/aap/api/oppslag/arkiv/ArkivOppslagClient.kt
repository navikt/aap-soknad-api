package no.nav.aap.api.oppslag.arkiv

import no.nav.aap.arkiv.DokumentInfoId
import org.springframework.stereotype.Component

@Component
class ArkivOppslagClient(private val a: ArkivOppslagWebClientAdapter) {
    fun dokument(journalpostId: String, dokumentId: String) =
        a.dokument(journalpostId, dokumentId)

    fun dokumenter() = a.dokumenter().sortedByDescending { it.dato }
}