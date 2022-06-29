package no.nav.aap.api.søknad

import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.api.søknad.routing.Router
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import javax.validation.Valid

@ProtectedRestController(value = ["/innsending"], issuer = IDPORTEN)
internal class InnsendingController(private val router: Router) {

    @PostMapping("/utland")
    @ResponseStatus(CREATED)
    fun utland(@RequestBody søknad: @Valid UtlandSøknad) = router.route(søknad)

    @PostMapping("/soknad")
    @ResponseStatus(CREATED)
    fun soknad(@RequestBody søknad: @Valid StandardSøknad) = router.route(søknad)
    override fun toString() = "$javaClass.simpleName [router=$router]"
}