package no.nav.aap.api.oppslag.arbeid

import no.nav.aap.api.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.boot.convert.PeriodFormat
import org.springframework.boot.convert.PeriodStyle.SIMPLE
import org.springframework.web.util.UriBuilder
import java.net.URI
import java.time.LocalDate.now
import java.time.Period
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

@ConfigurationProperties(ARBEID)
@ConstructorBinding
class ArbeidConfig(baseUri: URI,
                   @DefaultValue(PATH) private val path: String,
                   @DefaultValue("true") enabled: Boolean,
                   @DefaultValue(PINGPATH) pingPath: String,
                   @DefaultValue(FEMÅR) @PeriodFormat(SIMPLE) private val tidTilbake: Period,
                   @DefaultValue("false") val sporingsinformasjon: Boolean) :
    AbstractRestConfig(baseUri, pingPath, ARBEID, enabled) {

    fun arbeidsforholdURI(b: UriBuilder) =
        b.path(path)
            .queryParam(HISTORIKK, false)
            .queryParam(SPORINGSINFORMASJON, sporingsinformasjon)
            .queryParam(FOM, now().minus(tidTilbake).format(ISO_LOCAL_DATE))
            .build()

    override fun toString() =
        "$javaClass.simpleName [baseUri=$baseUri,  path=$path, pingEndpoint=$pingEndpoint, tidTilbake=$tidTilbake]"

    companion object {
        const val PINGPATH = "internal/isAlive"
        const val ARBEID = "arbeidsforhold"
        const val PATH = "api/v1/arbeidstaker/arbeidsforhold"
        const val FOM = "ansettelsesperiodeFom"
        const val FEMÅR = "5y"
        const val SPORINGSINFORMASJON = "sporingsinformasjon"
        const val HISTORIKK = "historikk"
    }
}