package no.nav.aap.api.søknad.fordeling

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import no.nav.aap.api.config.GlobalBeanConfig.AbstractKafkaHealthIndicator.AbstractKafkaConfig
import no.nav.aap.api.søknad.fordeling.VLFordelingConfig.Companion.VL

@ConfigurationProperties(VL)
class VLFordelingConfig(
        @NestedConfigurationProperty val standard: VLTopicConfig,
        @NestedConfigurationProperty val ettersending: VLTopicConfig,
        @NestedConfigurationProperty val utland: VLTopicConfig,
        @DefaultValue("true") val enabled: Boolean) : AbstractKafkaConfig(VL,enabled) {

    data class VLTopicConfig(val topic: String, @DefaultValue("true") val enabled: Boolean)

    companion object {
        const val VL = "vl"
    }

    override fun topics() = listOf(standard.topic,ettersending.topic,utland.topic)

}