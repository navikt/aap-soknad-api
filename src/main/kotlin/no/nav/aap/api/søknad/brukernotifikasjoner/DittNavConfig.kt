package no.nav.aap.api.søknad.brukernotifikasjoner

import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavConfig.Companion.DITTNAV
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal.EPOST
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal.SMS
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.time.Duration

@ConfigurationProperties(DITTNAV)
@ConstructorBinding
data class DittNavConfig(@NestedConfigurationProperty val nais: NAISConfig,
                         @NestedConfigurationProperty val beskjed: TopicConfig,
                         @NestedConfigurationProperty val oppgave: TopicConfig,
                         @NestedConfigurationProperty val done: TopicConfig) {

    val app = nais.app
    val namespace = nais.namespace

    data class TopicConfig(val topic: String,
                           val varighet: Duration = DEFAULT_DURATION,
                           val enabled: Boolean = true,
                           val preferertekanaler: List<PreferertKanal> = listOf(SMS, EPOST),
                           val sikkerhetsnivaa: Int = DEFAULT_LEVEL,
                           val eksternVarsling: Boolean = true)

    data class NAISConfig(val namespace: String, val app: String)

    companion object {
        const val DITTNAV = "dittnav"
        private val DEFAULT_DURATION = Duration.ofDays(90)
        private const val DEFAULT_LEVEL = 3
    }
}