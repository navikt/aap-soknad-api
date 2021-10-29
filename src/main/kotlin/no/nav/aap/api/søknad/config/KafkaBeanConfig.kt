package no.nav.aap.api.søknad.config

import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.config.SslConfigs.*
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.core.KafkaTemplate

@Configuration
class KafkaBeanConfig {
    private val log = LoggerFactory.getLogger(KafkaBeanConfig::class.java)

    @Bean
    fun aivenKafkaProducerTemplate(cfg: KafkaConfig): KafkaOperations<String, String> {
        val config = mapOf(
            BOOTSTRAP_SERVERS_CONFIG to cfg.brokers,
            CLIENT_ID_CONFIG to "aap-soknad-producer",
            ACKS_CONFIG to "1",
            KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ) + securityConfig(cfg)

        return KafkaTemplate(DefaultKafkaProducerFactory(config))
    }

    private fun securityConfig(cfg: KafkaConfig) = mapOf(
        SECURITY_PROTOCOL_CONFIG to "SSL",
        SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to "", // Disable server host name verification
        SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
        SSL_KEYSTORE_TYPE_CONFIG to "PKCS12",
        SSL_TRUSTSTORE_LOCATION_CONFIG to cfg.truststorePath,
        SSL_TRUSTSTORE_PASSWORD_CONFIG to cfg.credstorePassword,
        SSL_KEYSTORE_LOCATION_CONFIG to cfg.keystorePath,
        SSL_KEYSTORE_PASSWORD_CONFIG to cfg.credstorePassword,
        SSL_KEY_PASSWORD_CONFIG to cfg.credstorePassword,
    )
}
