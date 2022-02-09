package no.nav.aap.api.oppslag.brukerinfo

import no.nav.aap.api.oppslag.pdl.PDLOperations
import no.nav.aap.util.Constants
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping

@ProtectedRestController(value = ["/oppslag"], issuer = Constants.IDPORTEN)
class OppslagController(val pdl: PDLOperations) {

    @GetMapping("/soeker")
    fun søker() = pdl.søker(true)
}