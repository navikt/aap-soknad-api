package no.nav.aap.api.søknad.dittnav
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import io.confluent.kafka.serializers.KafkaAvroSerializer

//@Configuration
class DittNavKafkaBeanConfig {
    @Bean
    @Qualifier("dittnav")
    fun dittNavProducerFactory(pf: ProducerFactory<NokkelInput, Any>) =
        // Clone the PF with a different Serializer
         DefaultKafkaProducerFactory<NokkelInput, Any>(HashMap(pf.configurationProperties)
            .apply {
                put(KEY_SERIALIZER_CLASS_CONFIG,KafkaAvroSerializer::class.java)
                put(VALUE_SERIALIZER_CLASS_CONFIG,KafkaAvroSerializer::class.java)

            })

    @Bean
    @Qualifier("dittnav")
    fun dittNavTemplate(@Qualifier("dittnav") pf: ProducerFactory<NokkelInput,Any>) = KafkaTemplate(pf)
}