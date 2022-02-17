package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder
import java.net.URI
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE


@ConfigurationProperties(prefix = "arbeidsforhold")
class ArbeidsforholdConfig @ConstructorBinding constructor(@DefaultValue(DEFAULT_BASE_URI) baseUri: URI,
                                                           @DefaultValue(DEFAULT_PING) pingPath: String,
                                                           @DefaultValue(V1_ARBEIDSTAKER_ARBEIDSFORHOLD) val arbeidsforholdPath: String,
                                                           @DefaultValue("true") enabled: Boolean,
                                                           @DefaultValue(TREÅR) val tidTilbake: Period,
                                                           private val sporingsinformasjon: Boolean) : AbstractRestConfig(baseUri, pingPath, enabled) {


    fun arbeidsforholdURI(b: UriBuilder, fom: LocalDate): URI {
        return b.path(arbeidsforholdPath)
            .queryParam(HISTORIKK, false)
            .queryParam(SPORINGSINFORMASJON, sporingsinformasjon)
            .queryParam(FOM, fom.format(ISO_LOCAL_DATE))
            .build()
    }

    companion object {
        const val ARBEIDSFORHOLD = "arbeidsforhold"
        private const val DEFAULT_PING = "ping"
        private const val TREÅR = "3y"
        private const val V1_ARBEIDSTAKER_ARBEIDSFORHOLD = "/v1/arbeidstaker/arbeidsforhold"
        const val FOM = "ansettelsesperiodeFom"
        const val TOM = "ansettelsesperiodeTom"
        const val SPORINGSINFORMASJON = "sporingsinformasjon"
        const val HISTORIKK = "historikk"
        private const val DEFAULT_BASE_URI = "http://must.be.set"
    }
}