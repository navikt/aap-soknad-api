package no.nav.aap.api.søknad.routing


import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory


@Configuration
class RoutingBeanConfig {
   @Bean
    fun standardSøknadTemplate(pf: ProducerFactory<String, StandardSøknad>) = KafkaTemplate(pf)
    @Bean
    fun utenlandsSøknadTemplate(pf: ProducerFactory<String, UtlandSøknad>) = KafkaTemplate(pf)
}