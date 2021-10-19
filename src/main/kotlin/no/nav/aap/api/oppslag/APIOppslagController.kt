package no.nav.aap.api.oppslag

import no.nav.security.token.support.spring.ProtectedRestController
import no.nav.aap.api.søknad.rest.AbstractRestConfig
import no.nav.aap.api.søknad.pdl.PDLService
import org.springframework.web.bind.annotation.GetMapping
import no.nav.aap.api.søknad.domain.Søker
import no.nav.aap.api.søknad.tokenx.AuthContext

@ProtectedRestController(value = ["/api"], issuer = AbstractRestConfig.ISSUER)
class APIOppslagController(private val authContext: AuthContext, private val pdl: PDLService) {
    @GetMapping(path = ["me"])
    fun søker(): Søker {
        return Søker(authContext.fnr, pdl.navn())
    }

    override fun toString() = "${javaClass.simpleName} [tokenUtil=$authContext, pdl=$pdl]"
}
