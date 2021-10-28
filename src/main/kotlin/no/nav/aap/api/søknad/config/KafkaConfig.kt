package no.nav.aap.api.søknad.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties("kafka")
data class KafkaConfig @ConstructorBinding constructor (val brokers: String,
                       @NestedConfigurationProperty val truststorePath: TrustStore,
                       @NestedConfigurationProperty val credstorePassword: CredStore,
                       @NestedConfigurationProperty val keystorePath: KeyStore)

data class TrustStore(var path: String)
data class KeyStore(var path: String)
data class CredStore(var password: String)


