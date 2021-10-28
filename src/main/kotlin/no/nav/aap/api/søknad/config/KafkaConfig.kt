package no.nav.aap.api.søknad.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties("kafka")
class KafkaConfig @ConstructorBinding constructor (val brokers: String,
                                                   @NestedConfigurationProperty val truststorePath: TrustStore,
                                                   @NestedConfigurationProperty val credstorePassword: CredStore,
                                                   @NestedConfigurationProperty val keystorePath: KeyStore)

open  abstract class Store( protected val path: String)
data class TrustStore(val p: String) : Store(p)
data class KeyStore(val p: String) : Store(p)
data class CredStore(val password: String)


