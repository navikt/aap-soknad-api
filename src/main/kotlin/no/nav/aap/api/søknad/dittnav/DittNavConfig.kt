package no.nav.aap.api.søknad.dittnav

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration


@ConfigurationProperties(prefix = "dittnav")
@ConstructorBinding
class DittNavConfig(val topics: DittNavTopics,  @DefaultValue("90d") val beskjedVarighet: Duration) {
    data class DittNavTopics(val beskjed: String)
}