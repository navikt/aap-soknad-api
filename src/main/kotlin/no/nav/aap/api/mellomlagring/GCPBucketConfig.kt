package no.nav.aap.api.mellomlagring

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties("buckets")
@ConstructorBinding
class GCPBucketConfig(@DefaultValue("aap-mellomlagring") val mellomlagring: String,
                      @DefaultValue("aap-vedlegg") val vedlegg: String,
                      val kekuri: String)