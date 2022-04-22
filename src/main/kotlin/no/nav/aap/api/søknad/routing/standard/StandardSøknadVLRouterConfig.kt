package no.nav.aap.api.søknad.routing.standard

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue


@ConfigurationProperties(prefix = "vl")
@ConstructorBinding
class StandardSøknadVLRouterConfig(@DefaultValue(DEFAULT_VL_TOPIC) val topic: String) {

    companion object {
        private const val DEFAULT_VL_TOPIC = "aap.aap-soknad-sendt-ny.v1"
    }
}