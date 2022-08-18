package no.nav.aap.api.søknad.brukernotifikasjoner

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS
import org.springframework.stereotype.Component

@Configuration
class DittNavBeanConfig {

    @Bean
    fun dittNavKafkaOperations(pf: ProducerFactory<Any, Any>) =
        // Clone the PF to use Avro serializers
        KafkaTemplate(DefaultKafkaProducerFactory<NokkelInput, Any>(HashMap(pf.configurationProperties)
            .apply {
                put(KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
                put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
            }))

    @Bean
    fun notifikasjonConsumerFactory(kafkaProperties: KafkaProperties) =
        DefaultKafkaConsumerFactory<Any, Any>(kafkaProperties.buildConsumerProperties().apply {
            put(KEY_DESERIALIZER_CLASS, StringDeserializer::class.java)
            put(VALUE_DESERIALIZER_CLASS, KafkaAvroDeserializer::class.java)
        })

    @Bean
    fun notifikasjonListenerContainerFactory(kafkaConsumerFactory: ConsumerFactory<Any, Any>) =
        ConcurrentKafkaListenerContainerFactory<Any, Any>().apply {
            consumerFactory = kafkaConsumerFactory
        }

    @Component
    class EksternNotifikasjonStatusKonsument {
        private val log = getLogger(javaClass)

        @KafkaListener(topics = ["teamdokumenthandtering.aapen-dok-notifikasjon-status"],
                containerFactory = "notifikasjonListenerContainerFactory")
        fun consume(kafkaRecord: ConsumerRecord<Any, Any>) {
            with(kafkaRecord) {
                log.info("Notifikasjon:  key er ${key()}, value = ${value()}")
                //log.info("Notifikasjon:  key er ${kafkaRecord.key()}, bestiller= $bestillerId, bestillingId=$bestillingsId, status=$status, distribusjonId=$distribusjonId, melding=$melding}")
            }
        }
    }
}