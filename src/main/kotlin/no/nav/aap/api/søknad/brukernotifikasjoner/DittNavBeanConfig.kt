package no.nav.aap.api.søknad.brukernotifikasjoner

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.doknotifikasjon.schemas.DoknotifikasjonStatus
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID.fromString

@Configuration
class DittNavBeanConfig {
    private val log = getLogger(javaClass)

    @Bean
    fun dittNavKafkaOperations(properties: KafkaProperties) =
        KafkaTemplate(DefaultKafkaProducerFactory<NokkelInput, Any>(properties.buildProducerProperties()
            .apply {
                put(KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
                put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
            }))

    @Bean
    fun notifikasjonListenerContainerFactory(properties: KafkaProperties,
                                             @Value("\${spring.application.name}") appNavn: String) =
        ConcurrentKafkaListenerContainerFactory<String, DoknotifikasjonStatus>().apply {
            consumerFactory =
                DefaultKafkaConsumerFactory(properties.buildConsumerProperties().apply {
                    put(KEY_DESERIALIZER_CLASS, StringDeserializer::class.java)
                    put(VALUE_DESERIALIZER_CLASS, KafkaAvroDeserializer::class.java)
                    put(SPECIFIC_AVRO_READER_CONFIG, true)
                    setRecordFilterStrategy { payload ->
                        with(payload.value()) {
                            !(bestillerId == appNavn && status == FERDIGSTILT && melding.contains(NOTIFIKASJON_SENDT))
                        }
                    }
                })
        }

    @Component
    class EksternNotifikasjonStatusKonsument(private val repos: DittNavRepositories) {
        private val log = getLogger(javaClass)

        @KafkaListener(topics = ["teamdokumenthandtering.aapen-dok-notifikasjon-status"],
                containerFactory = "notifikasjonListenerContainerFactory")
        @Transactional
        fun listen(@Payload payload: DoknotifikasjonStatus) = oppdaterDistribusjonStatus(payload)

        private fun oppdaterDistribusjonStatus(payload: DoknotifikasjonStatus) {
            with(payload) {
                log.trace("Oppdaterer beskjed fra distribusjonsinfo fra $this")
                when (repos.beskjeder.distribuert(fromString(bestillingsId), melding, distribusjonId)) {
                    0 -> oppdaterOppgave(payload)
                    1 -> log.trace("Oppdatert beskjed $bestillingsId med distribusjonsinfo fra $this")
                    else -> log.warn("Uventet antall rader oppdatert for $bestillingsId med distribusjonsinfo fra $this (skal aldri skje)")
                }
            }
        }

        private fun oppdaterOppgave(payload: DoknotifikasjonStatus) {
            with(payload) {
                log.trace("Oppdaterer oppgave fra distribusjonsinfo fra $this")
                when (repos.oppgaver.distribuert(fromString(bestillingsId), melding, distribusjonId)) {
                    0 -> log.warn("Kunne  ikke oppdatere oppgave $bestillingsId med distribusjonsinfo fra $this")
                    1 -> log.trace("Oppdatert oppgave $bestillingsId med distribusjonsinfo fra $this")
                    else -> log.warn("Uventet antall rader oppdatert  for $bestillingsId  med distribusjonsinfo fra $this (skal aldri skje)")
                }
            }
        }
    }

    companion object {
        private const val FERDIGSTILT = "FERDIGSTILT"
        private const val NOTIFIKASJON_SENDT = "notifikasjon sendt via"
    }
}