package no.nav.aap.api.oppslag

import no.nav.aap.api.config.Constants.IDPORTEN
import no.nav.aap.api.felles.Søker
import no.nav.aap.api.oppslag.pdl.PDLOperations
import no.nav.aap.util.AuthContext
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping

@ProtectedRestController(value = ["/api"], issuer = IDPORTEN)
class APIOppslagController(private val ctx: AuthContext, private val pdl: PDLOperations) {

    @GetMapping("me")
    fun søker() = Søker(ctx.getFnr(), pdl.navn())

    override fun toString() = "${javaClass.simpleName} [authContext=$ctx, pdl=$pdl]"
}