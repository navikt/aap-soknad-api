package no.nav.aap.api.søknad

import no.nav.aap.api.søknad.routing.legacy.LegacyStandardSøknadKafkaRouter
import no.nav.aap.api.søknad.routing.standard.StandardSøknadRouter
import no.nav.aap.api.søknad.routing.utland.UtlandSøknadRouter
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtenlandsSøknad
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid

@ProtectedRestController(value = ["/innsending"], issuer = IDPORTEN)
internal class InnsendingController(
        private val legacyFormidler: LegacyStandardSøknadKafkaRouter,
        private val utenlandsFormidler: UtlandSøknadRouter,
        private val standardFormidler: StandardSøknadRouter) {

    @PostMapping("/utland")
    fun utland(@RequestBody søknad: @Valid UtenlandsSøknad) = utenlandsFormidler.formidle(søknad)
    @PostMapping("/soknad")
    fun legacy() = legacyFormidler.formidle()
    @PostMapping("/soknadny")
    fun standardNy(@RequestBody søknad: @Valid StandardSøknad) =standardFormidler.formidle(søknad)
    override fun toString() =
        "$javaClass.simpleName [standardFormidler=$standardFormidler,utenlandsFormidler=$utenlandsFormidler]"
}