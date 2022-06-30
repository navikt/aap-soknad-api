package no.nav.aap.api.søknad.mellomlagring

import no.nav.aap.api.søknad.mellomlagring.GCPBucketConfig.Companion.BUCKETS
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConfigurationProperties(BUCKETS)
@ConstructorBinding
class GCPBucketConfig(@DefaultValue("aap-mellomlagring") val mellomlagring: String,
                      @DefaultValue("aap-vedlegg") val vedlegg: String,
                      @DefaultValue("3000ms") val timeout: Duration,
                      val kms: String,
                      var typer: List<String>,
                      val kekuri: String) {
    companion object {
        const val BUCKETS = "buckets"
    }

    open class DokumentException(msg: String?, cause: Exception? = null) : RuntimeException(msg, cause)

}