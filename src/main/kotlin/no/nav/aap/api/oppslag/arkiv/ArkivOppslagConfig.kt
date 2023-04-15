package no.nav.aap.api.oppslag.arkiv

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAF
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT

@ConfigurationProperties(SAF)
class ArkivOppslagConfig(baseUri: URI, pingPath: String = PINGPATH, private val dokPath: String = DOKPATH,
        @NestedConfigurationProperty private val retryCfg: RetryConfig = DEFAULT,
        enabled: Boolean = true) : AbstractRestConfig(baseUri, pingPath, SAF, enabled,retryCfg) {

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