package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI


@ConfigurationProperties(prefix = "arbeidsforhold")
@ConstructorBinding
class ArbeidsforholdConfig(@DefaultValue("https://aareg-services-q1.dev.intern.nav.no/aareg-services") baseUri: URI): AbstractRestConfig(baseUri, "ping", true) {

    companion object {
        const val ARBEIDSFORHOLD = "arbeidsforhold"
    }
}