package no.nav.aap.api.søknad.arkiv

import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.util.Constants.JOARK
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConfigurationProperties(JOARK)
@ConstructorBinding
class ArkivConfig(
    @DefaultValue(DEFAULT_OPPRETT_PATH) val arkivPath: String,
    @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
    @DefaultValue("true") enabled: Boolean,
    baseUri: URI
) : AbstractRestConfig(baseUri, pingPath, JOARK, enabled) {

    override fun toString() =
        "${javaClass.simpleName} [pingPath=$pingPath,arkivPath=$arkivPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        const val CLIENT_CREDENTIALS_ARKIV = "client-credentials-arkiv"
        private const val DEFAULT_OPPRETT_PATH = "rest/journalpostapi/v1/journalpost"
        private const val DEFAULT_PING_PATH = "joark/ping"
    }
}