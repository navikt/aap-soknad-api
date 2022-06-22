package no.nav.aap.api.mellomlagring

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties("mellomlagring")
@ConstructorBinding
class GCPBucketConfig(@DefaultValue("aap-mellomlagring") val bucket: String, val kekuri: String)