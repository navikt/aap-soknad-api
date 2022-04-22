package no.nav.aap.api.søknad

import no.nav.aap.api.søknad.formidling.legacy.LegacyStandardSøknadKafkaFormidler
import no.nav.aap.api.søknad.formidling.standard.StandardSøknadFormidler
import no.nav.aap.api.søknad.formidling.utland.UtlandSøknadFormidler
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtenlandsSøknad
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid

@ProtectedRestController(value = ["/innsending"], issuer = IDPORTEN)
internal class InnsendingController(
        private val legacyFormidler: LegacyStandardSøknadKafkaFormidler,
        private val utenlandsFormidler: UtlandSøknadFormidler,
        private val standardFormidler: StandardSøknadFormidler) {

    @PostMapping("/utland")
    fun utland(@RequestBody søknad: @Valid UtenlandsSøknad) = utenlandsFormidler.formidle(søknad)
    @PostMapping("/soknad")
    fun legacy() = legacyFormidler.formidle()
    @PostMapping("/soknadny")
    fun standardNy(@RequestBody søknad: @Valid StandardSøknad) =standardFormidler.formidle(søknad)
    override fun toString() =
        "$javaClass.simpleName [standardFormidler=$standardFormidler,utenlandsFormidler=$utenlandsFormidler]"
}