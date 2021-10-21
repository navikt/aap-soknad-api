package no.nav.aap.api.oppslag

import no.nav.aap.api.config.Constants.ISSUER
import no.nav.aap.api.pdl.PdlOperations
import no.nav.aap.api.util.AuthContext
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping

@ProtectedRestController(value = ["/api"], issuer = ISSUER)
class APIOppslagController(private val authContext: AuthContext, private val pdl: PdlOperations) {
    @GetMapping("me")
    fun søker(): Søker {
        return Søker(authContext.getFnr(), pdl.navn())
    }

    override fun toString() = "${javaClass.simpleName} [tokenUtil=$authContext, pdl=$pdl]"
}
