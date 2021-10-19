package no.nav.aap.api.innsending

import no.nav.aap.api.søknad.domain.Kvittering
import no.nav.aap.api.søknad.domain.UtenlandsSøknad
import no.nav.aap.api.søknad.rest.AbstractRestConfig
import no.nav.aap.api.søknad.tokenx.AuthContext
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid

@ProtectedRestController(value = ["/api/innsending"], issuer = AbstractRestConfig.ISSUER)
class SøknadInnsendingController(private val authContext: AuthContext) {
    @PostMapping("/utland")
    fun utland(@RequestBody søknad: @Valid UtenlandsSøknad?): Kvittering {
        return Kvittering("OK")
    }

    override fun toString() = "${javaClass.simpleName} [authContext=$authContext]"
}