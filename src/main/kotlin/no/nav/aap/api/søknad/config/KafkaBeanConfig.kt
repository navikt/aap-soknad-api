package no.nav.aap.api.søknad.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate

@Configuration
class KafkaBeanConfig {
    @Bean
    fun aivenKafkaProducerTemplate(cfg: KafkaConfig): KafkaTemplate<String, String> {
        val config = mapOf(
            ProducerConfig.CLIENT_ID_CONFIG to "aap-soknad-producer",
            ProducerConfig.ACKS_CONFIG to "1",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ) + commonConfig(cfg)

        return KafkaTemplate(DefaultKafkaProducerFactory(config))
    }

    private fun commonConfig(cfg: KafkaConfig) = mapOf(
        BOOTSTRAP_SERVERS_CONFIG to cfg.kafkaBrokers
    ) + securityConfig(cfg)

    private fun securityConfig(cfg: KafkaConfig) = mapOf(
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SSL",
        SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to "", // Disable server host name verification
        SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
        SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "PKCS12",
        SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to cfg.kafkaTruststorePath,
        SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to cfg.kafkaCredstorePassword,
        SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to cfg.kafkaKeystorePath,
        SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to cfg.kafkaCredstorePassword,
        SslConfigs.SSL_KEY_PASSWORD_CONFIG to cfg.kafkaCredstorePassword,
    )
}
