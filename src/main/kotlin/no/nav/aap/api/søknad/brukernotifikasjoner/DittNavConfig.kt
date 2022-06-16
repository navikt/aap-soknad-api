package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavConfig.Companion.DITTNAV
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConfigurationProperties(DITTNAV)
@ConstructorBinding
data class DittNavConfig(@NestedConfigurationProperty val nais: NAISConfig,
                         @NestedConfigurationProperty val beskjed: TopicConfig,
                         @NestedConfigurationProperty val oppgave: TopicConfig,
                         @NestedConfigurationProperty val done: TopicConfig,
                         val mellomlagring: Long) {

    val app = nais.app
    val namespace = nais.namespace

    data class TopicConfig(val topic: String,
                           @DefaultValue(DEFAULT_VARIGHET) val varighet: Duration,
                           @DefaultValue("true") val enabled: Boolean,
                           @DefaultValue("{'SMS,EPOST}") val preferertekanaler: List<PreferertKanal>, // = listOf(SMS,
            //
                           @DefaultValue(DEFAULT_LEVEL) val sikkerhetsnivaa: Int,
                           @DefaultValue("true") val eksternVarsling: Boolean)

    data class NAISConfig(val namespace: String, val app: String)

    companion object {
        private const val DEFAULT_VARIGHET = "90d"
        const val DITTNAV = "dittnav"
        private const val DEFAULT_LEVEL = "3"
    }
}