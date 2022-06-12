package no.nav.aap.api.config

import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("nais")
@ConstructorBinding
@ConditionalOnGCP
data class NAISConfig(val namespace: String/*, val app: NAISApp, val cluster: NAISCluster*/) /*{
    data class NAISApp(val name: String, val image: String)
    data class NAISCluster(val name: String)
}*/