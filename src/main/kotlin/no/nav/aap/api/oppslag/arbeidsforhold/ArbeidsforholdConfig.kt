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
class ArbeidsforholdConfig(baseUri: URI,
                           @DefaultValue(PATH) private val path: String,
                           @DefaultValue("true") enabled: Boolean,
                           @DefaultValue(DEFAULT_PING)  pingPath: String,
                           @DefaultValue(FEMÅR) @PeriodFormat(SIMPLE) private val tidTilbake: Period,
                           @DefaultValue("false")  val sporingsinformasjon: Boolean): AbstractRestConfig(baseUri, pingPath, enabled) {

    fun arbeidsforholdURI(b: UriBuilder) =
        b.path(path)
             .queryParam(HISTORIKK, false)
            .queryParam(SPORINGSINFORMASJON, sporingsinformasjon)
            .queryParam(FOM, LocalDate.now().minus(tidTilbake).format(ISO_LOCAL_DATE))
             .build()

    companion object {
        const val DEFAULT_PING = "internal/isAlive"
        const val ARBEIDSFORHOLD = "arbeidsforhold"
        const val PATH = "api/v1/arbeidstaker/arbeidsforhold"
        const val FOM = "ansettelsesperiodeFom"
        const val FEMÅR = "5y"
        const val SPORINGSINFORMASJON = "sporingsinformasjon"
        const val HISTORIKK = "historikk"
    }
}