package no.nav.aap.api.søknad.dittnav

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.net.URL
import java.time.Duration
@ConfigurationProperties(prefix = "dittnav")
@ConstructorBinding
class DittNavConfig(val beskjed: DittNavTopicConfig) {
    data class DittNavTopicConfig(val topic: String,
                                  var landingsside: URL,
                                  val varighet: Duration,
                                  val tekst: String,
                                  val enabled: Boolean = true,
                                  val grupperingsId: String)
}