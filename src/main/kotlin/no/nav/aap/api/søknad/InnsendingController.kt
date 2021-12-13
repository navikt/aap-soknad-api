package no.nav.aap.api.søknad

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
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

@ProtectedRestController(value = ["/api/innsending"], issuer = IDPORTEN)
@SecurityRequirement(name = "bearerAuth")
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer")
class InnsendingController(
        private val authContext: AuthContext,
        private val formidler: SøknadFormidler) {
    private val log = LoggerUtil.getLogger(javaClass)

    @PostMapping("/utland")
    fun utland(@RequestBody søknad: @Valid UtenlandsSøknadView): Kvittering {
        log.info(CONFIDENTIAL, "Sender søknad for {}", authContext.getFnr())
        formidler.sendUtenlandsSøknad(authContext.getFnr(), søknad)
        return Kvittering("OK")
    }

    override fun toString() = "${javaClass.simpleName} [authContext=$authContext]"
}