package no.nav.aap.api.søknad.formidling


import no.nav.aap.api.felles.UtenlandsSøknadKafka
import no.nav.aap.api.søknad.model.StandardSøknad
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory


@Configuration
class KafkaBeanConfig {
    @Bean
    fun legacySøknadTemplate(pf: ProducerFactory<String, LegacyStandardSøknadKafka>) = KafkaTemplate(pf)

    @Bean
    fun standardSøknadTemplate(pf: ProducerFactory<String, StandardSøknad>) = KafkaTemplate(pf)

    @Bean
    fun utenlandsSøknadTemplate(pf: ProducerFactory<String, UtenlandsSøknadKafka>) = KafkaTemplate(pf)

}