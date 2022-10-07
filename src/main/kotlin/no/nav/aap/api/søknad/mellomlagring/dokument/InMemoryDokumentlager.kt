package no.nav.aap.api.søknad.mellomlagring.dokument

import java.util.*
import no.nav.aap.api.søknad.model.StandardSøknad
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean

@ConditionalOnMissingBean(GCPKryptertDokumentlager::class)
class InMemoryDokumentlager : Dokumentlager {
    private val store = mutableMapOf<String, String>()

    override fun lesDokument(uuid: UUID) = null

    override fun slettDokumenter(vararg uuids: UUID): Unit? {
        TODO("Not yet implemented")
    }

    override fun slettDokumenter(søknad: StandardSøknad) {
        TODO("Not yet implemented")
    }

    override fun lagreDokument(dokument: DokumentInfo) = UUID.randomUUID()
}