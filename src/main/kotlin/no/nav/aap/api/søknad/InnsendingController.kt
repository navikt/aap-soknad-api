package no.nav.aap.api.søknad

import jakarta.validation.Valid
import no.nav.aap.api.søknad.fordeling.Fordeler
import no.nav.aap.api.søknad.model.Innsending
import no.nav.aap.api.søknad.model.StandardEttersending
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus

@ProtectedRestController(value = ["/innsending"], issuer = IDPORTEN)
@ResponseStatus(CREATED)
class InnsendingController(private val fordeler: Fordeler) {

    @PostMapping("/utland")
    fun utland(@RequestBody @Valid søknad: UtlandSøknad) = fordeler.fordel(søknad)

    @PostMapping("/soknad")
    fun soknad(@RequestBody @Valid innsending: Innsending) = fordeler.fordel(innsending)

    @PostMapping("/ettersend")
    fun ettersend(@RequestBody @Valid ettersending: StandardEttersending) = fordeler.fordel(ettersending)

    override fun toString() = "$javaClass.simpleName [fordeler=$fordeler]"
}