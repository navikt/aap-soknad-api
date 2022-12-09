package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.config.GlobalBeanConfig.AbstractKafkaHealthIndicator.AbstractKafkaConfig
import no.nav.aap.api.søknad.fordeling.VLFordelingConfig.Companion.VL
import no.nav.aap.api.søknad.minside.MinSideConfig.KafkaConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(VL)
@ConstructorBinding
class VLFordelingConfig(
        @NestedConfigurationProperty val standard: VLTopicConfig,
        @NestedConfigurationProperty val ettersending: VLTopicConfig,
        @NestedConfigurationProperty val utland: VLTopicConfig,
        @DefaultValue("true") val enabled: Boolean) : AbstractKafkaConfig(VL,enabled) {

    class VLTopicConfig( topic: String, @DefaultValue("true")  enabled: Boolean) : KafkaConfig(topic,enabled)

    companion object {
        const val VL = "vl"
    }

    override fun topics() = listOf(standard,ettersending,utland)

}