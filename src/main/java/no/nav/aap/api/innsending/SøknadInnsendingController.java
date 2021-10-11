package no.nav.aap.api.innsending;

import no.nav.aap.api.søknad.domain.Kvittering;
import no.nav.aap.api.søknad.domain.UtenlandsSøknad;
import no.nav.aap.api.søknad.tokenx.AuthContext;
import no.nav.security.token.support.spring.ProtectedRestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

import static no.nav.aap.api.søknad.rest.AbstractRestConfig.ISSUER;

@ProtectedRestController(value = "/api/innsending", issuer = ISSUER)
public class SøknadInnsendingController {

    private final AuthContext authContext;

    public SøknadInnsendingController(AuthContext authContext) {
        this.authContext = authContext;
    }

    @PostMapping("/utland")
    public Kvittering utland(@Valid @RequestBody UtenlandsSøknad søknad) {
        return new Kvittering();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [tokenUtil=" + authContext + "]";
    }
}
