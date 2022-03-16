package no.nav.aap.api.oppslag.organisasjon

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder
import java.net.URI
import org.springframework.web.util.UriComponentsBuilder.newInstance


@ConfigurationProperties(prefix = "organisasjon")
@ConstructorBinding
class OrganisasjonConfig (baseUri: URI,
                          @DefaultValue(ORGPATH) private val organisasjonPath: String,
                          @DefaultValue("true") enabled: Boolean) :
    AbstractRestConfig(baseUri, pingPath(organisasjonPath), enabled) {
    fun getOrganisasjonURI(b: UriBuilder, orgnr: String?): URI {
        return b.path(organisasjonPath)
            .queryParam("orgnummer", orgnr)
            .build(orgnr)
    }

    override fun toString(): String {
        return javaClass.simpleName + "[organisasjonPath=" + organisasjonPath + ", pingEndpoint=" + pingEndpoint + "]"
    }

    companion object {
        const val ORGANISASJON = "Organisasjon"
        private const val ORGPATH = "organisasjon"
        private const val NAV = "998004993"
        private fun pingPath(organisasjonPath: String): String {
            return newInstance()
                .path(organisasjonPath)
                .queryParam("orgnummer", NAV)
                .toString()
        }
    }
}