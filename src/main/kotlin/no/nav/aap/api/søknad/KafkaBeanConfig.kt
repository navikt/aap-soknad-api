package no.nav.aap.api.søknad


import no.nav.aap.api.felles.UtenlandsSøknadKafka
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory


@Configuration
class KafkaBeanConfig

    @Bean
    fun søknadTemplate(pf: ProducerFactory<String, SøknadKafka>) = KafkaTemplate(pf)

    @Bean
    fun utenlandsSøknadTemplate(pf: ProducerFactory<String, UtenlandsSøknadKafka>) = KafkaTemplate(pf)