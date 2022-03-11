package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.boot.convert.PeriodFormat
import org.springframework.boot.convert.PeriodStyle.SIMPLE
import org.springframework.web.util.UriBuilder
import java.net.URI
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE


@ConfigurationProperties(prefix = "arbeidsforhold")
@ConstructorBinding
class ArbeidsforholdConfig(@DefaultValue(DEFAULT_URI) baseUri: URI,
                           @DefaultValue(PATH) val path: String,
                           @DefaultValue("true") enabled: Boolean,
                           @DefaultValue(DEFAULT_PING)  pp: String,
                           @DefaultValue(FEMÅR) @PeriodFormat(SIMPLE) val tidTilbake: Period,
                           @DefaultValue("false")  val sporingsinformasjon: Boolean): AbstractRestConfig(baseUri, pp, enabled) {

    fun arbeidsforholdURI(b: UriBuilder, fom: LocalDate) =
        b.path(path)
             .queryParam(HISTORIKK, false)
            .queryParam(SPORINGSINFORMASJON, sporingsinformasjon)
            .queryParam(FOM, fom.format(ISO_LOCAL_DATE))
             .build()

    companion object {
        const val DEFAULT_PING = "actuator/health/liveness"
        const val DEFAULT_URI  ="https://aap-fss-proxy.dev-fss-pub.nais.io"
        const val ARBEIDSFORHOLD = "arbeidsforhold"
        const val PATH = ARBEIDSFORHOLD
        const val FOM = "ansettelsesperiodeFom"
        const val TOM = "ansettelsesperiodeTom"
        const val FEMÅR = "5y"
        const val SPORINGSINFORMASJON = "sporingsinformasjon"
        const val HISTORIKK = "historikk"
    }
}