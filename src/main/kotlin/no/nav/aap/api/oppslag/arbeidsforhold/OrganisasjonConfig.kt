package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.api.oppslag.arbeidsforhold.OrganisasjonConfig.Companion.ORGANISASJON
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder
import java.net.URI


@ConfigurationProperties(ORGANISASJON)
@ConstructorBinding
class OrganisasjonConfig(baseUri: URI,
                         @DefaultValue(ORGPATH) private val organisasjonPath: String,
                         @DefaultValue(PINGPATH) pingPath: String,
                         @DefaultValue("true") enabled: Boolean) : AbstractRestConfig(baseUri, pingPath, enabled) {

    fun orgURI(b: UriBuilder, orgnr: OrgNummer) =
        b.path(organisasjonPath)
            .queryParam("orgnummer", orgnr.orgnr)
            .build()


    override fun toString() =
        "$javaClass.simpleName [baseUri=$baseUri,  organisasjonPath=$organisasjonPath, pingEndpoint=$pingEndpoint]"

    companion object {
        const val ORGANISASJON = "organisasjon"
        private const val ORGPATH = "organisasjon"
        private const val PINGPATH = ORGPATH + "/ping"
    }
}