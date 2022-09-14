package no.nav.aap.api.oppslag.arkiv

import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAF
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder
import java.net.URI

@ConfigurationProperties(SAF)
@ConstructorBinding
class ArkivOppslagConfig(
    baseUri: URI,
    @DefaultValue(PINGPATH) pingPath: String,
    @DefaultValue(DOKPATH) private val dokPath: String,
    @DefaultValue("true") enabled: Boolean
                        ) : AbstractRestConfig(baseUri, pingPath, SAF, enabled) {

    fun dokUri(b: UriBuilder, journalpostId: String, dokumentInfoId: String, variant: String) =
        b.path(dokPath).build(journalpostId, dokumentInfoId, variant)

    override fun toString() =
        "$javaClass.simpleName [baseUri=$baseUri,  dokPath=$dokPath, pingEndpoint=$pingEndpoint]"

    companion object {
        const val SAKER_QUERY = "query-saf.graphql"
        const val SAF = "saf"
        private const val PINGPATH = ""
        private const val DOKPATH = "rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}"
    }
}