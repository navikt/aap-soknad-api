package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavConfig.Companion.DITTNAV
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConfigurationProperties(DITTNAV)
@ConstructorBinding
data class DittNavConfig(val beskjed: TopicConfig, val oppgave: TopicConfig, val done: TopicConfig) {
    data class TopicConfig(val topic: String,
                           val varighet: Duration = DEFAULT_DURATION,
                           val enabled: Boolean = true,
                           val sikkerhetsnivaa: Int = DEFAULT_LEVEL,
                           val eksternVarsling: Boolean = false)

    companion object {
        const val DITTNAV = "dittnav"
        private val DEFAULT_DURATION = Duration.ofDays(90)
        private const val DEFAULT_LEVEL = 3
    }
}