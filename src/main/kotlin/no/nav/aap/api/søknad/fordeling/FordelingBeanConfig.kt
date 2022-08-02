package no.nav.aap.api.søknad.fordeling

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class FordelingBeanConfig {
    @Bean
    fun vlFordelingTemplate(pf: ProducerFactory<String, Any>) = KafkaTemplate(pf)
}