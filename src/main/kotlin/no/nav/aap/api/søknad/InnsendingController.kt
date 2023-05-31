package no.nav.aap.api.søknad

import io.micrometer.observation.annotation.Observed
import jakarta.validation.Valid
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import no.nav.aap.api.søknad.fordeling.Ettersending
import no.nav.aap.api.søknad.fordeling.Fordeler
import no.nav.aap.api.søknad.fordeling.Innsending
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController

@ProtectedRestController(value = ["/innsending"], issuer = IDPORTEN)
@ResponseStatus(CREATED)
@Observed
class InnsendingController(private val fordeler : Fordeler) {

    @PostMapping("/soknad")
    fun soknad(@RequestBody @Valid innsending : Innsending) = fordeler.fordel(innsending)

    @PostMapping("/ettersend")
    fun ettersend(@RequestBody @Valid ettersending : Ettersending) = fordeler.fordel(ettersending)

    override fun toString() = "$javaClass.simpleName [fordeler=$fordeler]"
}