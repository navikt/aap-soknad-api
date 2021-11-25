package no.nav.aap.api.søknad

import no.nav.aap.api.config.Constants.IDPORTEN
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
import no.nav.aap.api.util.AuthContext
import no.nav.aap.api.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid

@ProtectedRestController(value = ["/api/innsending"], issuer = IDPORTEN)
class SøknadController(
    private val authContext: AuthContext,
    private val formidler: SøknadFormidler
) {
    private val log = getLogger(javaClass)
    @PostMapping("/utland")
    fun utland(@RequestBody søknad: @Valid UtenlandsSøknadView): Kvittering {
        log.info(CONFIDENTIAL,"Sender søknad for {}",authContext.getFnr())
        formidler.sendUtenlandsSøknad(authContext.getFnr(), søknad)
        return Kvittering("OK")
    }
    override fun toString() = "${javaClass.simpleName} [authContext=$authContext]"
}