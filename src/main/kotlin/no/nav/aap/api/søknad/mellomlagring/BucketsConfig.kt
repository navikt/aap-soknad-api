package no.nav.aap.api.søknad.mellomlagring

import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.Companion.BUCKETS
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.time.Duration

@ConfigurationProperties(BUCKETS)
@ConstructorBinding
data class BucketsConfig(@NestedConfigurationProperty val mellom: BucketCfg,
                         @NestedConfigurationProperty val vedlegg: VedleggBucketCfg,
                         val id: String,
                         val kms: KeyConfig) {

    data class KeyConfig(val ring: String, val key: String)

    open class BucketCfg(val navn: String,
                         val subscription: String,
                         val topic: String,
                         val timeout: Duration = Duration.ofSeconds(30),
                         val kms: String) {
        override fun toString() =
            "MellomBucketCfg(navn=$navn, subscription=$subscription, timeout=${timeout.toSeconds()}s, kms=$kms)"
    }

    class VedleggBucketCfg(navn: String,
                           subscription: String,
                           topic: String,
                           timeout: Duration = Duration.ofSeconds(30),
                           kms: String,
                           val typer: List<String>) : BucketCfg(navn, subscription, topic, timeout, kms) {
        override fun toString() =
            "VedleggBucketCfg(navn=$navn, subscription=$subscription, timeout=${timeout.toSeconds()}s, kms=$kms,typer=$typer)"
    }

    companion object {
        const val REGION = "europe-north1"
        const val BUCKETS = "buckets"
    }
}

open class DokumentException(msg: String?, cause: Exception? = null) : RuntimeException(msg, cause)