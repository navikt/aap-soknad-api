package no.nav.aap.api.søknad.dittnav

import no.nav.aap.api.søknad.dittnav.DittNavConfig.Companion.DITTNAV
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConfigurationProperties(DITTNAV)
@ConstructorBinding
data class DittNavConfig(val beskjed: TopicConfig, val oppgave: TopicConfig) {
    data class TopicConfig(val topic: String, val varighet: Duration, val enabled: Boolean = true)

    companion object {
        const val DITTNAV = "dittnav"
    }
}