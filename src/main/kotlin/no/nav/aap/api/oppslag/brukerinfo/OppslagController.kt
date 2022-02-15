package no.nav.aap.api.oppslag.brukerinfo

import no.nav.aap.api.felles.Navn
import no.nav.aap.api.oppslag.fastlege.Fastlege
import no.nav.aap.api.oppslag.fastlege.FastlegeClient
import no.nav.aap.api.oppslag.pdl.PDLOperations
import no.nav.aap.util.Constants
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping

@ProtectedRestController(value = ["/oppslag"], issuer = Constants.IDPORTEN)
class OppslagController(val pdl: PDLOperations, val fastlege: FastlegeClient) {

    @GetMapping("/soeker")
    fun søker(): SøkerInfo = SøkerInfo(pdl.søker(true), Fastlege(Navn("Ikke", "implementert","enda")))
}