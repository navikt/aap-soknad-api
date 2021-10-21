package no.nav.aap.api.søknad

import no.nav.aap.api.config.Constants.ISSUER
import no.nav.aap.api.util.AuthContext
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid


@ProtectedRestController(value = ["/api/innsending"], issuer = ISSUER)
class SøknadInnsendingController(private val authContext: AuthContext) {
    @PostMapping("/utland")
    fun utland(@RequestBody søknad: @Valid UtenlandsSøknad): Kvittering {
        return Kvittering("OK")
    }

    override fun toString() = "${javaClass.simpleName} [authContext=$authContext]"
}