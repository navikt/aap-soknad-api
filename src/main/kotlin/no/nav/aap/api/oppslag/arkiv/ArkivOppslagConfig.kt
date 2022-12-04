package no.nav.aap.api.oppslag.arkiv

import java.net.URI
import java.time.Duration
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAF
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(SAF)
@ConstructorBinding
class ArkivOppslagConfig(
        baseUri: URI,
        @DefaultValue(PINGPATH) pingPath: String,
        @DefaultValue(DOKPATH) private val dokPath: String,
        @DefaultValue("3")  retries: Long,
        @DefaultValue("100ms")  delay: Duration,
        @DefaultValue("true") enabled: Boolean) : AbstractRestConfig(baseUri, pingPath, SAF, enabled,retries,delay) {

    fun dokUri() = "$baseUri$dokPath"

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