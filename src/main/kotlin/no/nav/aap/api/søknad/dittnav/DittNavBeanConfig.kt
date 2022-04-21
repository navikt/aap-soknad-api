package no.nav.aap.api.søknad.dittnav
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG
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

@Configuration
class DittNavBeanConfig {
    @Bean
    @Qualifier("dittnav")
    fun dittNavKafkaTemplate(pf: ProducerFactory<Any, Any>) =
        // Clone the PF with a different Serializer
         KafkaTemplate(DefaultKafkaProducerFactory<NokkelInput, Any>(HashMap(pf.configurationProperties)
            .apply {
                put(KEY_SERIALIZER_CLASS_CONFIG,KafkaAvroSerializer::class.java)
                put(VALUE_SERIALIZER_CLASS_CONFIG,KafkaAvroSerializer::class.java)
                put(SPECIFIC_AVRO_READER_CONFIG,true)
            }))
}