package no.nav.aap.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties("nais")
@ConstructorBinding
data class NAISConfig(@DefaultValue("local") val namespace: String, val app: NAISApp, val cluster: NAISCluster) {
    data class NAISApp(val name: String, val image: String)
    data class NAISCluster(val name: String)
}