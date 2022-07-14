package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.kms.v1.CryptoKeyName
import com.google.cloud.kms.v1.LocationName
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.Companion.BUCKETS
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConfigurationProperties(BUCKETS)
@ConstructorBinding
data class BucketsConfig(val id: String,
                         @NestedConfigurationProperty val mellom: MellomlagringBucketConfig,
                         @NestedConfigurationProperty val vedlegg: VedleggBucketConfig,
                         @NestedConfigurationProperty @DefaultValue val kms: KeyConfig) {
    val kryptoKey = with(kms) {
        CryptoKeyName.of(id, LocationName.of(id, REGION).location, ring, key).toString()
    }

    data class KeyConfig(val ring: String, val key: String)

    data class MellomlagringBucketConfig(val navn: String,
                                         @NestedConfigurationProperty val subscription: SubscriptionConfig,
                                         @DefaultValue(DEFAULT_TIMEOUT) val timeout: Duration) {

        data class SubscriptionConfig(val navn: String, val topic: String)
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