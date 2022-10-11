package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.config.BeanConfig.AbstractKafkaHealthIndicator.AbstractKafkaConfig
import no.nav.aap.api.søknad.fordeling.VLFordelingConfig.Companion.VL
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(VL)
@ConstructorBinding
class VLFordelingConfig(
        @NestedConfigurationProperty val standard: VLTopicConfig,
        @NestedConfigurationProperty val ettersending: VLTopicConfig,
        @NestedConfigurationProperty val utland: VLTopicConfig) : AbstractKafkaConfig(VL,standard.enabled || ettersending.enabled || utland.enabled) {

    data class VLTopicConfig(val topic: String, @DefaultValue("true") val enabled: Boolean)

    companion object {
        const val VL = "vl"
    }

    override fun topics() = listOf(standard.topic,ettersending.topic,utland.topic)

}