package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.kms.v1.CryptoKeyName
import com.google.cloud.kms.v1.LocationName
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.Companion.BUCKETS
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConfigurationProperties(BUCKETS)
@ConstructorBinding
data class BucketsConfig(@NestedConfigurationProperty val mellom: MellomlagringBucketConfig,
                         @NestedConfigurationProperty val vedlegg: VedleggBucketConfig,
                         val id: String,
                         @NestedConfigurationProperty @DefaultValue val kms: KeyConfig) {
    @Value("\${spring.application.name}")
    lateinit var name: String
    val kryptoKey = CryptoKeyName.of(id, LocationName.of(id, REGION).location, kms.ring, kms.nøkkel).toString()

    data class KeyConfig(val ring: String = "aap-mellomlagring-kms",
                         val nøkkel: String = "aap-mellomlagring-kms-key")

    data class MellomlagringBucketConfig(val navn: String,
                                         @NestedConfigurationProperty @DefaultValue val subscription: SubscriptionConfig,
                                         @DefaultValue(DEFAULT_TIMEOUT) val timeout: Duration) {

        data class SubscriptionConfig(val navn: String = "aap-mellomlagring-subscription",
                                      val topic: String = "aap-mellomlagring-topic")
    }

    data class VedleggBucketConfig(val navn: String,
                                   @DefaultValue(DEFAULT_TIMEOUT) val timeout: Duration,
                                   val typer: List<String>) {
    }

    companion object {
        const val DEFAULT_TIMEOUT = "30s"
        const val REGION = "europe-north1"
        const val BUCKETS = "buckets"
    }
}

open class DokumentException(msg: String?, cause: Exception? = null) : RuntimeException(msg, cause)