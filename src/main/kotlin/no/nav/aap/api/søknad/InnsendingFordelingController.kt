package no.nav.aap.api.søknad

import no.nav.aap.api.søknad.fordeling.Fordeler
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import javax.validation.Valid

@ProtectedRestController(value = ["/innsending"], issuer = IDPORTEN)
class InnsendingFordelingController(private val fordeler: Fordeler) {

    @PostMapping("/utland")
    @ResponseStatus(CREATED)
    fun utland(@RequestBody søknad: @Valid UtlandSøknad) = fordeler.fordel(søknad)

    @PostMapping("/soknad")
    @ResponseStatus(CREATED)
    fun soknad(@RequestBody søknad: @Valid StandardSøknad) = fordeler.fordel(søknad)
    override fun toString() = "$javaClass.simpleName [fordeler=$fordeler]"
}