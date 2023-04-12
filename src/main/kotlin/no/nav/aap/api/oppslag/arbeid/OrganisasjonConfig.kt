package no.nav.aap.api.oppslag.arbeid

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder
import org.springframework.web.util.UriComponentsBuilder
import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.util.Constants.ORGANISASJON

@ConfigurationProperties(ORGANISASJON)
class OrganisasjonConfig(baseUri: URI,
                         @DefaultValue(V1_ORGANISASJON) private val organisasjonPath: String,
                         @NestedConfigurationProperty private val retryCfg: RetryConfig = DEFAULT,
                         @DefaultValue("true") enabled: Boolean) :
    AbstractRestConfig(baseUri, pingPath(organisasjonPath), ORGANISASJON, enabled,retryCfg) {

    constructor(baseUri: URI) : this(baseUri, V1_ORGANISASJON,DEFAULT,true)

    fun organisasjonURI(b: UriBuilder, orgnr: OrgNummer) = b.path(organisasjonPath).build(orgnr.orgnr)

    companion object {
        private const val V1_ORGANISASJON = "v1/organisasjon/{orgnr}"
        private const val TESTORG = "947064649"
        private fun pingPath(organisasjonPath: String) =
            UriComponentsBuilder.newInstance()
                .path(organisasjonPath)
                .build(TESTORG)
                .toString()
    }

    override fun toString() =
        "${javaClass.simpleName} [organisasjonPath=" + organisasjonPath + ", pingEndpoint=" + pingEndpoint + "]"
}