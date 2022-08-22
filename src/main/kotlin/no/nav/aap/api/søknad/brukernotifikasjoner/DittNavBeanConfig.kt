package no.nav.aap.api.søknad.brukernotifikasjoner

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavNotifikasjonRepository.EksternNotifikasjon
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
    fun dittNavKafkaOperations(props: KafkaProperties) =
        KafkaTemplate(DefaultKafkaProducerFactory<NokkelInput, Any>(props.buildProducerProperties()
            .apply {
                put(KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
                put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
            }))

    @Bean
    fun notifikasjonListenerContainerFactory(props: KafkaProperties,
                                             @Value("\${spring.application.name}") appNavn: String) =
        ConcurrentKafkaListenerContainerFactory<String, DoknotifikasjonStatus>().apply {
            consumerFactory =
                DefaultKafkaConsumerFactory(props.buildConsumerProperties().apply {
                    put(KEY_DESERIALIZER_CLASS, StringDeserializer::class.java)
                    put(VALUE_DESERIALIZER_CLASS, KafkaAvroDeserializer::class.java)
                    put(SPECIFIC_AVRO_READER_CONFIG, true)
                    setRecordFilterStrategy { payload ->
                        with(payload.value()) {
                            val status =
                                !(bestillerId == appNavn && status == FERDIGSTILT && melding.contains(NOTIFIKASJON_SENDT))
                            log.trace("Record filter status for $payload er $status")
                            status
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
                log.trace("Oppdaterer oppgave med distribusjonsinfo fra $this")
                repos.oppgaver.findOppgaveByEventid(fromString(bestillingsId))?.let { n ->
                    n.notifikasjoner.add(EksternNotifikasjon(
                            oppgave = n,
                            eventid = fromString(bestillingsId),
                            distribusjonid = distribusjonId,
                            distribusjonkanal = melding))
                    repos.oppgaver.save(n).also {
                        log.trace("Oppdatert oppgave med $n i $it i DB")
                    }
                }

                when (repos.beskjeder.distribuert(fromString(bestillingsId), melding, distribusjonId)) {
                    0 -> oppdaterOppgave(payload)
                    1 -> log.trace("Oppdatert beskjed $bestillingsId med distribusjonsinfo fra $this")
                    else -> log.warn("Uventet antall rader oppdatert for $bestillingsId med distribusjonsinfo fra $this (skal aldri skje)")
                }
            }
        }

        private fun oppdaterOppgave(payload: DoknotifikasjonStatus) {
            with(payload) {
                log.trace("Oppdaterer oppgave med distribusjonsinfo fra $this")
                val oppgave = repos.oppgaver.findOppgaveByEventid(fromString(bestillingsId))
                log.trace("XXXX  Fant oppgave $oppgave")
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