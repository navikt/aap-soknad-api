package no.nav.aap.api.søknad

import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.util.Constants
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus

@ProtectedRestController(value = ["/migrering"], issuer = Constants.AAD)
@ResponseStatus(HttpStatus.CREATED)
class MigreringRestController (private val søknadRepository: SøknadRepository)
{
    @GetMapping("/søknader")
    fun hentSøknader() = søknadRepository.findAll()

}