package no.nav.aap.api.søknad

import no.nav.aap.api.config.Constants.ISSUER
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
import no.nav.aap.api.util.AuthContext
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid

@ProtectedRestController(value = ["/api/innsending"], issuer = ISSUER)
class SøknadController(
    private val authContext: AuthContext,
    private val søknadKafkaProducer: SøknadKafkaProducer
) {
    @PostMapping("/utland")
    fun utland(@RequestBody søknad: @Valid UtenlandsSøknadView): Kvittering {
        søknadKafkaProducer.sendUtlandsSøknad(authContext.getFnr(ISSUER).fnr, søknad)
        return Kvittering("OK")
    }

    override fun toString() = "${javaClass.simpleName} [authContext=$authContext]"
}
