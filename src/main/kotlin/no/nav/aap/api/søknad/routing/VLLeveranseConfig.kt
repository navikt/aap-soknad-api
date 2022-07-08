package no.nav.aap.api.søknad.routing

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "vl")
@ConstructorBinding
class VLLeveranseConfig(
        @NestedConfigurationProperty val standard: VLTopicConfig = VLTopicConfig(DEFAULT_VL_TOPIC, true),
        @NestedConfigurationProperty val utland: VLTopicConfig = VLTopicConfig(DEFAULT_VL_UTLAND_TOPIC, true)) {

    data class VLTopicConfig(val topic: String, val enabled: Boolean)

    companion object {
        private const val DEFAULT_VL_TOPIC = "aap.soknad-sendt.v1"
        private const val DEFAULT_VL_UTLAND_TOPIC = "aap.utland-soknad-sendt.v1"
    }
}