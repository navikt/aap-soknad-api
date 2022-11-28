package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.kms.v1.CryptoKeyName
import com.google.cloud.kms.v1.KeyRingName
import com.google.cloud.kms.v1.LocationName
import java.time.Duration
import no.nav.aap.api.error.Substatus
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.BUCKETS
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.util.unit.DataSize

@ConfigurationProperties(BUCKETS)
data class BucketConfig(val project: String,
                        @NestedConfigurationProperty val mellom: MellomlagringBucketConfig,
                        @NestedConfigurationProperty val vedlegg: VedleggBucketConfig,
                        @NestedConfigurationProperty val kms: KeyConfig) {

    val location = LocationName.of(project, REGION)
    val ring = KeyRingName.of(project, location.location, kms.ring)
    val key = CryptoKeyName.of(project, location.location, kms.ring, kms.key)

    data class KeyConfig(internal val ring: String, internal val key: String)

    data class MellomlagringBucketConfig(val navn: String, @NestedConfigurationProperty val purring: Purring, @NestedConfigurationProperty val subscription: SubscriptionConfig) {

        data class SubscriptionConfig(val navn: String, val topic: String)
        data class Purring(val enabled: Boolean, val delay: Long, val eldreEnn: Duration)
    }

    data class VedleggBucketConfig(val navn: String, val maxsum: DataSize, val typer: List<String>)

    companion object {
        private const val REGION = "europe-north1"
        const val BUCKETS = "buckets"
        const val UUID_ = "uuid"
        const val SKJEMATYPE = "skjemaType"
    }
}

open class DokumentException(msg: String?, cause: Throwable? = null, val substatus: Substatus? = null) : RuntimeException(msg, cause)