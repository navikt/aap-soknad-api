package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.kms.v1.LocationName
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.Companion.BUCKETS
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(BUCKETS)
@ConstructorBinding
data class BucketsConfig(val project: String,
                         @NestedConfigurationProperty val mellom: MellomlagringBucketConfig,
                         @NestedConfigurationProperty val vedlegg: VedleggBucketConfig,
                         @NestedConfigurationProperty val kms: KeyConfig) {

    val location = LocationName.of(project, REGION)

    data class KeyConfig(internal val ring: String, internal val key: String)

    data class MellomlagringBucketConfig(val navn: String,
                                         @NestedConfigurationProperty val subscription: SubscriptionConfig) {

        data class SubscriptionConfig(val navn: String, val topic: String)
    }

    data class VedleggBucketConfig(val navn: String,
                                   val typer: List<String>)

    companion object {
        const val REGION = "europe-north1"
        const val BUCKETS = "buckets"
    }
}

open class DokumentException(msg: String?, cause: Exception? = null) : RuntimeException(msg, cause)