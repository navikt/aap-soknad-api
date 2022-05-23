package no.nav.aap.api.oppslag.saf

import no.nav.aap.api.oppslag.krr.KRRConfig.Companion.KRR
import no.nav.aap.api.oppslag.saf.SafConfig.Companion.SAF
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder
import java.net.URI

@ConfigurationProperties(SAF)
@ConstructorBinding
class SafConfig(baseUri: URI,
                @DefaultValue(PINGPATH) pingPath: String,
                @DefaultValue(DOKPATH) private val dokPath: String,
                @DefaultValue("true") enabled: Boolean) : AbstractRestConfig(baseUri, pingPath, KRR, enabled) {

    fun dokUri(b: UriBuilder, journalpostId: String, dokumentInfoId: String, variant: String) =
        b.path(dokPath).build(journalpostId, dokumentInfoId, variant)

    override fun toString() =
        "$javaClass.simpleName [baseUri=$baseUri,  dokPath=$dokPath, pingEndpoint=$pingEndpoint]"

    companion object {
        const val SAF = "saf"
        private const val PINGPATH = "/isAlive"
        private const val DOKPATH = "/rest/hentdokument/\${journalpostId}/\${dokumentInfoId}/\${variantFormat}"
    }
}