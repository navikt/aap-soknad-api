package no.nav.aap.api.søknad.minside

import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.aap.api.config.GlobalBeanConfig.AbstractKafkaHealthIndicator
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.minside.MinSideConfig.Companion.MINSIDE
import no.nav.aap.api.søknad.minside.MinSideEksternNotifikasjonStatusKonsument.Companion.DOKNOTIFIKASJON
import no.nav.aap.api.søknad.minside.MinSideEksternNotifikasjonStatusKonsument.Companion.FEILET
import no.nav.aap.api.søknad.minside.MinSideEksternNotifikasjonStatusKonsument.Companion.FERDIGSTILT
import no.nav.aap.api.søknad.minside.MinSideEksternNotifikasjonStatusKonsument.Companion.NOTIFIKASJON_SENDT
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.doknotifikasjon.schemas.DoknotifikasjonStatus
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Component

@Configuration
class MinSideBeanConfig(@Value("\${spring.application.name}") private val appNavn: String) {
    private val log = getLogger(javaClass)

    @Component
    @ConditionalOnProperty("$MINSIDE.enabled", havingValue = "true")
    class MinsidePingable(admin: KafkaAdmin, p: KafkaProperties, cfg: MinSideConfig) : AbstractKafkaHealthIndicator(admin,p.bootstrapServers,cfg)

    @Bean
    @ConditionalOnProperty("$MINSIDE.enabled", havingValue = "true")
    fun minsideHealthIndicator(adapter: MinsidePingable) = object : AbstractPingableHealthIndicator(adapter) {}
    @Bean
    fun minSideKafkaOperations(p: KafkaProperties) =
        KafkaTemplate(DefaultKafkaProducerFactory<NokkelInput, Any>(p.buildProducerProperties()
            .apply {
                put(KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
                put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
            }))
    @Bean
    fun minSideForsideKafkaOperations(p: KafkaProperties) =
        KafkaTemplate(DefaultKafkaProducerFactory<Fødselsnummer, MinSideForside>(p.buildProducerProperties()
            .apply {
                put(KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer::class.java)
                put(VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer::class.java)
            }))

    @Bean
    fun minSideUtkastKafkaOperations(p: KafkaProperties) =
        KafkaTemplate(DefaultKafkaProducerFactory<String, String>(p.buildProducerProperties()
            .apply {
                put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
                put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
            }))
    @Bean(DOKNOTIFIKASJON)
    fun dokNotifikasjonListenerContainerFactory(p: KafkaProperties) =
        ConcurrentKafkaListenerContainerFactory<String, DoknotifikasjonStatus>().apply {
            consumerFactory = DefaultKafkaConsumerFactory(p.buildConsumerProperties().apply {
                setRecordFilterStrategy(::recordFilterStrategy)
            })
        }

    private fun recordFilterStrategy(payload: ConsumerRecord<String, DoknotifikasjonStatus>) =
        with(payload.value()) {
            when (bestillerId) {
                appNavn -> {
                    when (status) {
                        FERDIGSTILT -> !melding.contains(NOTIFIKASJON_SENDT)

                        FEILET ->
                            true.also {
                                log.warn("Ekstern notifikasjon feilet for bestillingid $bestillingsId, ($melding)")
                            }

                        else ->
                            true.also {
                                log.trace("Ekstern notifikasjon status $status filtrert vekk for bestillingid $bestillingsId")
                            }
                    }
                }
                else -> true
            }
        }
  }