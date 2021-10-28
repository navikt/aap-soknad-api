package no.nav.aap.api.søknad.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties("kafka")
class KafkaConfig @ConstructorBinding constructor (val brokers: String,
                                                   @NestedConfigurationProperty val kafkaTruststorePath: TrustStore,
                                                   @NestedConfigurationProperty val kafkaCredstorePassword: CredStore,
                                                   @NestedConfigurationProperty val kafkaKeystorePath: KeyStore)

data class TrustStore(val path: String)
data class KeyStore(val path: String)
data class CredStore(val password: String)


