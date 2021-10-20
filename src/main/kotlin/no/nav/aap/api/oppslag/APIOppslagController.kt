package no.nav.aap.api.oppslag

import no.nav.aap.api.søknad.config.Constants.ISSUER
import no.nav.aap.api.søknad.domain.Søker
import no.nav.aap.api.søknad.pdl.PDLClient
import no.nav.aap.api.søknad.tokenx.AuthContext
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping

@ProtectedRestController(value = ["/api"], issuer = ISSUER)
class APIOppslagController(private val authContext: AuthContext, private val pdl: PDLClient) {
    @GetMapping("me")
    fun søker(): Søker {
        return Søker(authContext.getFnr(), pdl.navn())
    }

    override fun toString() = "${javaClass.simpleName} [tokenUtil=$authContext, pdl=$pdl]"
}
