package no.nav.aap.api.mellomlagring

import no.nav.aap.api.felles.Fødselsnummer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import java.util.*

@ConditionalOnMissingBean(GCPMellomlager::class)
class InMemoryDokumentlager : Dokumentlager {
    private val store = mutableMapOf<String, String>()

    override fun lesDokument(fnr: Fødselsnummer, uuid: UUID) = null

    override fun slettDokument(fnr: Fødselsnummer, uuid: UUID) = true

    override fun lagreDokument(fnr: Fødselsnummer,
                               bytes: ByteArray,
                               contentType: String?,
                               originalFilename: String?) = UUID.randomUUID()
}