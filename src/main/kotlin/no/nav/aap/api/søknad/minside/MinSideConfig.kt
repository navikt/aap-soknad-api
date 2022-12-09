package no.nav.aap.api.søknad.minside

import java.net.URI
import java.time.Duration
import no.nav.aap.api.config.GlobalBeanConfig.AbstractKafkaHealthIndicator.AbstractKafkaConfig
import no.nav.aap.api.søknad.minside.MinSideConfig.Companion.MINSIDE
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import org.aspectj.weaver.tools.cache.SimpleCacheFactory.enabled
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(MINSIDE)
@ConstructorBinding
data class MinSideConfig(@NestedConfigurationProperty private val nais: NAISConfig,
                         @NestedConfigurationProperty val beskjed: TopicConfig,
                         @NestedConfigurationProperty val oppgave: TopicConfig,
                         @NestedConfigurationProperty val utkast: UtkastConfig,
                         @NestedConfigurationProperty val backlinks: BacklinksConfig,
                         val done: String) : AbstractKafkaConfig(MINSIDE,enabled) {
          val enabled = utkast.isEnabled || oppgave.isEnabled || beskjed.isEnabled

    val app = nais.app
    val namespace = nais.namespace


    class UtkastConfig(@DefaultValue(UTKAST_TOPIC)  val topic: String, @DefaultValue("true")  val enabled: Boolean) : KafkaConfig(topic,enabled)

    data class BacklinksConfig(val innsyn: URI, val standard: URI, val utland: URI)
    class TopicConfig(topic: String,
                           @DefaultValue(DEFAULT_VARIGHET) val varighet: Duration,
                           @DefaultValue("true")  val enabled: Boolean,
                           val preferertekanaler: List<PreferertKanal> = emptyList(),
                           @DefaultValue(DEFAULT_LEVEL) val sikkerhetsnivaa: Int)  : KafkaConfig(topic,enabled)

    abstract  class KafkaConfig(val topicName: String, val isEnabled: Boolean)


    data class NAISConfig(val namespace: String, val app: String)

    companion object {
        private const val UTKAST_TOPIC = "minside.aapen-utkast-v1"
        private const val DEFAULT_VARIGHET = "14d"
        const val MINSIDE = "minside"
        private const val DEFAULT_LEVEL = "3"
    }

    override fun topics() =  listOf(utkast,beskjed,oppgave)

}