package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE


@ConfigurationProperties(prefix = "arbeidsforhold")
@ConstructorBinding
class ArbeidsforholdConfig(@DefaultValue(DEFAULT_URI) baseUri: URI,
                           @DefaultValue(PATH) val path: String,
                           @DefaultValue("true") enabled: Boolean,
                           private val sporingsinformasjon: Boolean): AbstractRestConfig(baseUri, "ping", enabled) {

    fun arbeidsforholdURI(b: UriBuilder, fom: LocalDate): URI {
        return b.path(path)
            .queryParam(HISTORIKK, false)
            .queryParam(SPORINGSINFORMASJON, sporingsinformasjon)
            .queryParam(FOM, fom.format(ISO_LOCAL_DATE))
            .build()
    }

    companion object {
        const val DEFAULT_URI  ="https://aareg-services-q1.dev.intern.nav.no/aareg-services"
        const val ARBEIDSFORHOLD = "arbeidsforhold"
        const val PATH = "api/v2/arbeidstaker/arbeidsforhold"
        const val FOM = "ansettelsesperiodeFom"
        const val TOM = "ansettelsesperiodeTom"
        const val SPORINGSINFORMASJON = "sporingsinformasjon"
        const val HISTORIKK = "historikk"
    }
}