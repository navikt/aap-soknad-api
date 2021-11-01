package no.nav.aap.api.søknad.config

import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

//@ConfigurationProperties("kafka")
//@ConditionalOnProperty("kafka.brokers")
data class KafkaConfig @ConstructorBinding constructor (val brokers: String,
                                                   @NestedConfigurationProperty val truststorePath: TrustStore,
                                                   @NestedConfigurationProperty val credstorePassword: CredStore,
                                                   @NestedConfigurationProperty val keystorePath: KeyStore)
data class TrustStore(val path: String)
data class KeyStore(val path: String)
data class CredStore(val password: String)