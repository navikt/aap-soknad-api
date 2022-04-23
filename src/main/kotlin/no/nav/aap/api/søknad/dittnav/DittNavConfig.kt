package no.nav.aap.api.søknad.dittnav

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration
@ConfigurationProperties(prefix = "dittnav")
@ConstructorBinding
data class DittNavConfig(val beskjed: TopicConfig) {
    data class TopicConfig(val topic: String, val varighet: Duration, val enabled: Boolean = true)
}