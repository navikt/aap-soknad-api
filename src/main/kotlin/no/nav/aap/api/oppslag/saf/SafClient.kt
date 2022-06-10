package no.nav.aap.api.oppslag.saf

import no.nav.aap.joark.DokumentInfoId
import org.springframework.stereotype.Component

@Component
class SafClient(private val a: SafWebClientAdapter) {
    fun dokument(journalpostId: String, dokumentId: DokumentInfoId) =
        a.dokument(journalpostId, dokumentId.dokumentInfoId)

    fun getDocument() = a.getDocument()
}