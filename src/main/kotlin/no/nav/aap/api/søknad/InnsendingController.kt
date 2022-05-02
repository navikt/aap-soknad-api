package no.nav.aap.api.søknad

import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.api.søknad.routing.standard.StandardSøknadRouter
import no.nav.aap.api.søknad.routing.utland.UtlandSøknadRouter
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid

@ProtectedRestController(value = ["/innsending"], issuer = IDPORTEN)
internal class InnsendingController(private val utlandRouter: UtlandSøknadRouter, private val standardRouter: StandardSøknadRouter) {

    @PostMapping("/utland")
    fun utland(@RequestBody søknad: @Valid UtlandSøknad) = utlandRouter.route(søknad)
    @PostMapping("/soknad")
    fun soknad(@RequestBody søknad: @Valid StandardSøknad) =standardRouter.route(søknad)
    override fun toString() =
        "$javaClass.simpleName [standardRouter=$standardRouter,utlandRouter=$utlandRouter]"
}