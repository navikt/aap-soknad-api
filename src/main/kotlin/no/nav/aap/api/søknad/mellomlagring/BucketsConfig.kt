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
data class BucketsConfig(@NestedConfigurationProperty val mellom: BucketCfg,
                         @NestedConfigurationProperty val vedlegg: VedleggBucketCfg,
                         val id: String,
                         @DefaultValue val kms: KeyConfig) {
    val kryptoKey = CryptoKeyName.of(id, LocationName.of(id, REGION).location, kms.ring, kms.nøkkel).toString()

    data class KeyConfig(val ring: String = "aap-mellomlagring-kms",
                         val nøkkel: String = "aap-mellomlagring-kms-key") {

    }

    open class BucketCfg(val navn: String,
                         val subscription: String,
                         val topic: String,
                         @DefaultValue(DEFAULT_TIMEOUT) val timeout: Duration) {
        override fun toString() =
            "MellomBucketCfg(navn=$navn, subscription=$subscription, timeout=${timeout.toSeconds()}s)"
    }

    class VedleggBucketCfg(val navn: String,
                           @DefaultValue(DEFAULT_TIMEOUT) val timeout: Duration,
                           val typer: List<String>) {
        override fun toString() =
            "VedleggBucketCfg(navn=$navn,  timeout=${timeout.toSeconds()}s, typer=$typer)"
    }

    companion object {
        const val DEFAULT_TIMEOUT = "30s"
        const val REGION = "europe-north1"
        const val BUCKETS = "buckets"
    }
}

open class DokumentException(msg: String?, cause: Exception? = null) : RuntimeException(msg, cause)