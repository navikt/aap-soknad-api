package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI
import java.time.Period


@ConfigurationProperties(prefix = "arbeidsforhold")
@ConstructorBinding
class ArbeidsforholdConfig(@DefaultValue("https://aareg-services-q1.dev.intern.nav.no/aareg-services") baseUri: URI,
                            @DefaultValue(DEFAULT_PING) pingPath: String,
                            @DefaultValue(PATH) val path: String,
                            @DefaultValue("true") enabled: Boolean,
                            @DefaultValue(FEMÅR) val tidTilbake: Period,
                            private val sporingsinformasjon: Boolean) : AbstractRestConfig(baseUri, pingPath, enabled) {


    /*
    fun arbeidsforholdURI(b: UriBuilder, fom: LocalDate): URI {
        return b.path(arbeidsforholdPath)
            .queryParam(HISTORIKK, false)
            .queryParam(SPORINGSINFORMASJON, sporingsinformasjon)
            .queryParam(FOM, fom.format(ISO_LOCAL_DATE))
            .build()
    }

     */

    companion object {
        const val ARBEIDSFORHOLD = "arbeidsforhold"
        private const val DEFAULT_PING = "ping"
        private const val FEMÅR = "5y"
        private const val PATH = "api/v2/arbeidstaker/arbeidsforhold"
        const val FOM = "ansettelsesperiodeFom"
        const val TOM = "ansettelsesperiodeTom"
        const val SPORINGSINFORMASJON = "sporingsinformasjon"
        const val HISTORIKK = "historikk"
    }
}