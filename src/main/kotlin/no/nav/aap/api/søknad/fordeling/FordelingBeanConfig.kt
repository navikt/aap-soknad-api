package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.config.BeanConfig.AbstractKafkaHealthIndicator
import no.nav.aap.api.søknad.fordeling.VLFordelingConfig.Companion.VL
import no.nav.aap.health.AbstractPingableHealthIndicator
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.stereotype.Component

@Configuration
class FordelingBeanConfig {

    @Component
    class VLPingable(admin: KafkaAdmin, p: KafkaProperties, cfg: VLFordelingConfig) : AbstractKafkaHealthIndicator(admin,p.bootstrapServers,cfg)

    @Bean
    @ConditionalOnProperty("$VL.enabled", havingValue = "true")
    fun vlHealthIndicator(adapter: VLPingable) = object : AbstractPingableHealthIndicator(adapter) {}

    @Bean
    fun vlFordelingTemplate(pf: ProducerFactory<String, Any>) = KafkaTemplate(pf)
}