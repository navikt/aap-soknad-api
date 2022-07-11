package no.nav.aap.api.søknad.mellomlagring

import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.Companion.BUCKETS
import org.springframework.beans.factory.annotation.Value
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

    data class KeyConfig(val ring: String = "aap-mellomlagring-kms",
                         val nøkkel: String = "aap-mellomlagring-kms-key",
                         @Value("#{\${spring.application.name}}") val jalla: String?)

    open class BucketCfg(val navn: String,
                         val subscription: String,
                         val topic: String,
                         @DefaultValue(DEFAULT_TIMEOUT) val timeout: Duration,
                         val kms: String) {
        override fun toString() =
            "MellomBucketCfg(navn=$navn, subscription=$subscription, timeout=${timeout.toSeconds()}s, kms=$kms)"
    }

    class VedleggBucketCfg(navn: String,
                           subscription: String,
                           topic: String,
                           @DefaultValue(DEFAULT_TIMEOUT) timeout: Duration,
                           kms: String,
                           val typer: List<String>) : BucketCfg(navn, subscription, topic, timeout, kms) {
        override fun toString() =
            "VedleggBucketCfg(navn=$navn, subscription=$subscription, timeout=${timeout.toSeconds()}s, kms=$kms,typer=$typer)"
    }

    companion object {
        const val DEFAULT_TIMEOUT = "30s"
        const val REGION = "europe-north1"
        const val BUCKETS = "buckets"
    }
}

open class DokumentException(msg: String?, cause: Exception? = null) : RuntimeException(msg, cause)