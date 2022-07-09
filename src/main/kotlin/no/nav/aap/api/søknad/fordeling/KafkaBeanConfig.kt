package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaBeanConfig {
    @Bean
    fun standardSøknadTemplate(pf: ProducerFactory<String, StandardSøknad>) = KafkaTemplate(pf)

    @Bean
    fun utlandSøknadTemplate(pf: ProducerFactory<String, UtlandSøknad>) = KafkaTemplate(pf)
}