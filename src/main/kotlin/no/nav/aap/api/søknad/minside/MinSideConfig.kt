package no.nav.aap.api.søknad.minside

import java.net.URI
import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import no.nav.aap.api.config.GlobalBeanConfig.AbstractKafkaHealthIndicator.AbstractKafkaConfig
import no.nav.aap.api.søknad.minside.MinSideConfig.Companion.MINSIDE
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal

@ConfigurationProperties(MINSIDE)
data class MinSideConfig(@NestedConfigurationProperty private val nais: NAISConfig,
                         @NestedConfigurationProperty val beskjed: TopicConfig,
                         @NestedConfigurationProperty val oppgave: TopicConfig,
                         @NestedConfigurationProperty val utkast: UtkastConfig,
                         @NestedConfigurationProperty val forside: ForsideConfig,
                         @NestedConfigurationProperty val backlinks: BacklinksConfig,
                         val enabled: Boolean = true, val done: String) : AbstractKafkaConfig(MINSIDE,enabled) {

    val app = nais.app
    val namespace = nais.namespace
    data class ForsideConfig( @DefaultValue("min-side.aapen-microfrontend-v1") val topic: String, @DefaultValue("true") val enabled: Boolean)
    data class UtkastConfig( @DefaultValue("min-side.aapen-utkast-v1") val topic: String, @DefaultValue("true") val enabled: Boolean)

    data class BacklinksConfig(val innsyn: URI, val standard: URI, val utland: URI)
    data class TopicConfig(val topic: String,
                           val varighet: Duration = Duration.ofDays(14),
                           val enabled: Boolean = true,
                           val preferertekanaler: List<PreferertKanal> = emptyList(),
                           @DefaultValue(DEFAULT_LEVEL) val sikkerhetsnivaa: Int)

    data class NAISConfig(val namespace: String, val app: String)

    companion object {
        const val MINSIDE = "minside"
        private const val DEFAULT_LEVEL = "3"
    }

    override fun topics() =  listOf(utkast.topic,beskjed.topic,oppgave.topic,done)

}