package no.nav.aap.api.søknad.mellomlagring

import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.Companion.BUCKETS
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.time.Duration

@ConfigurationProperties(BUCKETS)
@ConstructorBinding
data class BucketsConfig(@NestedConfigurationProperty val mellom: MellomBucketCfg,
                         @NestedConfigurationProperty val vedlegg: VedleggBucketCfg, val id: String) {

    open class MellomBucketCfg(val navn: String,
                               val subscription: String,
                               val timeout: Duration,
                               val kms: String) {
        override fun toString() =
            "MellomBucketCfg(navn=$navn, subscription=$subscription, timeout=${timeout.toSeconds()}s, kms=$kms)"
    }

    class VedleggBucketCfg(navn: String, subscription: String, timeout: Duration,
                           kms: String, val typer: List<String>) : MellomBucketCfg(navn, subscription, timeout, kms) {
        override fun toString() =
            "VedleggBucketCfg(navn=$navn, subscription=$subscription, timeout=${timeout.toSeconds()}s, kms=$kms,typer=$typer)"
    }

    companion object {
        const val BUCKETS = "buckets"
    }
}

open class DokumentException(msg: String?, cause: Exception? = null) : RuntimeException(msg, cause)