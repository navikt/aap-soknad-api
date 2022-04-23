package no.nav.aap.api.søknad.dittnav

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.net.URL
import java.time.Duration
@ConfigurationProperties(prefix = "dittnav")
@ConstructorBinding
data class DittNavConfig(val beskjed: TopicConfig) {
    data class TopicConfig(val topic: String,
                           var landingsside: URL,
                           val varighet: Duration,
                           val enabled: Boolean = true)
}