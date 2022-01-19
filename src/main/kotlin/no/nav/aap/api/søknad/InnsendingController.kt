package no.nav.aap.api.søknad

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.Søknad
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid

@ProtectedRestController(value = ["/innsending"], issuer = IDPORTEN)
@SecurityRequirement(name = "bearerAuth")
class InnsendingController(
        private val formidler: KafkaSøknadFormidler,
        private val utenlandsFormidler: KafkaUtenlandsSøknadFormidler) {

    @PostMapping("/utland")
    fun utland(@RequestBody søknad: @Valid UtenlandsSøknadView): Kvittering {
        utenlandsFormidler.formidle(søknad)
        return Kvittering("OK")
    }

    @PostMapping("/soknad")
    fun utland(@RequestBody søknad: @Valid Søknad): Kvittering {
        return Kvittering("OK")
    }

    override fun toString() = "${javaClass.simpleName} [formidler=$formidler,utenlandsFormidler=$utenlandsFormidler]"
}