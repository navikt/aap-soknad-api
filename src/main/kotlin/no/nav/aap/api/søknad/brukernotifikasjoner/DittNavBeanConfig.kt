package no.nav.aap.api.søknad.brukernotifikasjoner

import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.util.AuthContext
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import java.util.*

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
    fun auditorProvider(ctx: AuthContext) = AuditorAware { Optional.of(ctx.getFnr().fnr) }
}