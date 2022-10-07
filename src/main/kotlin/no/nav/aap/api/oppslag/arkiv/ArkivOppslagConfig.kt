package no.nav.aap.api.oppslag.arkiv

import java.net.URI
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAF
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagDokumentInfo.ArkivOppslagDokumentVariant.ArkivOppslagDokumentVariantFormat.ARKIV
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder

@ConfigurationProperties(SAF)
@ConstructorBinding
class ArkivOppslagConfig(
        baseUri: URI,
        @DefaultValue(PINGPATH) pingPath: String,
        @DefaultValue(DOKPATH) private val dokPath: String,
        @DefaultValue("true") enabled: Boolean) : AbstractRestConfig(baseUri, pingPath, SAF, enabled) {

    fun dokUri(b: UriBuilder, journalpostId: String, dokumentInfoId: String, variant: String = ARKIV.name) =
        b.path(dokPath).build(journalpostId, dokumentInfoId, variant)

    override fun toString() =
        "$javaClass.simpleName [baseUri=$baseUri,  dokPath=$dokPath, pingEndpoint=$pingEndpoint]"

    companion object {
        const val DOKUMENTER_QUERY = "query-dokumenter.graphql"
        const val SAF = "saf"
        const val SAFQL = "safql"
        private const val PINGPATH = "actuator/health/liveness"
        private const val DOKPATH = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}"
    }
}