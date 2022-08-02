package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.søknad.fordeling.VLFordelingConfig.Companion.VL
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(VL)
@ConstructorBinding
class VLFordelingConfig(
        @NestedConfigurationProperty val standard: VLTopicConfig = VLTopicConfig(DEFAULT_VL_TOPIC, true),
        @NestedConfigurationProperty val utland: VLTopicConfig = VLTopicConfig(DEFAULT_VL_UTLAND_TOPIC, true)) {

    data class VLTopicConfig(val topic: String, val enabled: Boolean)

    companion object {
        const val VL = "vl"
        private const val DEFAULT_VL_TOPIC = "aap.soknad-sendt.v1"
        private const val DEFAULT_VL_UTLAND_TOPIC = "aap.utland-soknad-sendt.v1"
    }
}