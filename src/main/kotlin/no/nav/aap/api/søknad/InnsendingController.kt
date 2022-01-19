package no.nav.aap.api.søknad

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid

@ProtectedRestController(value = ["/innsending"], issuer = IDPORTEN)
@SecurityRequirement(name = "bearerAuth")
class InnsendingController(
        private val authContext: AuthContext,
        private val formidler: KafkaSøknadFormidler,
        private val utenlandsFormidler: KafkaUtenlandsSøknadFormidler) {
    private val log = LoggerUtil.getLogger(javaClass)

    @PostMapping("/utland")
    fun utland(@RequestBody søknad: @Valid UtenlandsSøknadView): Kvittering {
        log.info(CONFIDENTIAL, "Formidler utenlandssøknad for {}", authContext.getFnr())
        utenlandsFormidler.formidle(authContext.getFnr(), søknad)
        return Kvittering("OK")
    }

    @PostMapping("/soknad")
    fun utland(@RequestBody søknad: @Valid Søknad): Kvittering {
        log.info(CONFIDENTIAL, "Formidler ssøknad for {}", authContext.getFnr())
        formidler.formidle(authContext.getFnr())
        return Kvittering("OK")
    }

    override fun toString() = "${javaClass.simpleName} [authContext=$authContext]"
}

class Søknad