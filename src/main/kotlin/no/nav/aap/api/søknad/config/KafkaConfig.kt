package no.nav.aap.api.søknad.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KafkaConfig(@Value("\${KAFKA_BROKERS}") val kafkaBrokers: String,
                  @Value("\${KAFKA_TRUSTSTORE_PATH}") val kafkaTruststorePath: String,
                  @Value("\${KAFKA_CREDSTORE_PASSWORD}") val kafkaCredstorePassword: String,
                  @Value("\${KAFKA_KEYSTORE_PATH}")  val kafkaKeystorePath: String)
