package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavConfig.Companion.DITTNAV
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URL
import java.time.Duration

@ConfigurationProperties(DITTNAV)
@ConstructorBinding
data class DittNavConfig(@NestedConfigurationProperty private val nais: NAISConfig,
                         @NestedConfigurationProperty val beskjed: TopicConfig,
                         @NestedConfigurationProperty val backlinks: BacklinksConfig,
                         @NestedConfigurationProperty val oppgave: TopicConfig,
                         @DefaultValue(DEFAULT_DONE) val done: String) {

    val app = nais.app
    val namespace = nais.namespace

    data class BacklinksConfig(val innsyn: URL, val standard: URL, val utland: URL)
    data class TopicConfig(val topic: String,
                           @DefaultValue(DEFAULT_VARIGHET) val varighet: Duration,
                           @DefaultValue("true") val enabled: Boolean,
                           val preferertekanaler: List<PreferertKanal> = listOf(),
                           @DefaultValue(DEFAULT_LEVEL) val sikkerhetsnivaa: Int,
                           @DefaultValue("true") val eksternVarsling: Boolean)

    data class NAISConfig(val namespace: String, val app: String)

    companion object {
        private const val DEFAULT_DONE = "min-side.aapen-brukernotifikasjon-done-v1"
        private const val DEFAULT_VARIGHET = "90d"
        const val DITTNAV = "dittnav"
        private const val DEFAULT_LEVEL = "3"
    }
}