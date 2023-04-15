package no.nav.aap.api.søknad.arkiv

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.util.Constants.JOARK

@ConfigurationProperties(JOARK)
class ArkivConfig(
        baseUri: URI,
        @NestedConfigurationProperty val hendelser: HendelseConfig,
        val arkivPath: String = DEFAULT_OPPRETT_PATH,
        pingPath: String = DEFAULT_PING_PATH,
        enabled: Boolean = true,
        @NestedConfigurationProperty private val retryCfg: RetryConfig =DEFAULT,
        ) : AbstractRestConfig(baseUri, pingPath, JOARK, enabled,retryCfg) {

    data class HendelseConfig(val topic: String)


    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,arkivPath=$arkivPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        const val ARKIVHENDELSER = "joarkhendelser"
        const val CLIENT_CREDENTIALS_ARKIV = "client-credentials-arkiv"
        private const val DEFAULT_OPPRETT_PATH = "rest/journalpostapi/v1/journalpost"
        private const val DEFAULT_PING_PATH = "actuator/health/liveness"
    }
}