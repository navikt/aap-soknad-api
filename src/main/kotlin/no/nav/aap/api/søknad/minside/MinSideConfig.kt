package no.nav.aap.api.søknad.minside

import no.nav.aap.api.søknad.minside.MinSideConfig.Companion.MINSIDE
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import org.apache.kafka.common.config.TopicConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI
import java.time.Duration

@ConfigurationProperties(MINSIDE)
@ConstructorBinding
data class MinSideConfig(@NestedConfigurationProperty private val nais: NAISConfig,
                         @NestedConfigurationProperty val beskjed: TopicConfig,
                         @NestedConfigurationProperty val backlinks: BacklinksConfig,
                         @NestedConfigurationProperty val oppgave: TopicConfig,
                         @DefaultValue("true") val enabled: Boolean,
                         @DefaultValue(DEFAULT_DONE) val done: String) {

    val app = nais.app
    val namespace = nais.namespace

    data class BacklinksConfig(val innsyn: URI, val standard: URI, val utland: URI)
    data class TopicConfig(val topic: String,
                           @DefaultValue(DEFAULT_VARIGHET) val varighet: Duration,
                           @DefaultValue("true") val enabled: Boolean,
                           val preferertekanaler: List<PreferertKanal> = emptyList(),
                           @DefaultValue(DEFAULT_LEVEL) val sikkerhetsnivaa: Int)

    data class NAISConfig(val namespace: String, val app: String)

    companion object {
        private const val DEFAULT_DONE = "min-side.aapen-brukernotifikasjon-done-v1"
        private const val DEFAULT_VARIGHET = "90d"
        const val MINSIDE = "minside"
        private const val DEFAULT_LEVEL = "3"
    }
}