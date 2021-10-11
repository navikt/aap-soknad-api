package no.nav.aap.api.oppslag;

import no.nav.aap.api.søknad.domain.Søker;
import no.nav.aap.api.søknad.pdl.PDLService;
import no.nav.aap.api.søknad.tokenx.AuthContext;
import no.nav.security.token.support.spring.ProtectedRestController;
import org.springframework.web.bind.annotation.GetMapping;

import static no.nav.aap.api.søknad.rest.AbstractRestConfig.ISSUER;


@ProtectedRestController(value = "/api", issuer = ISSUER)
public class APIOppslagController {

    private final AuthContext authContext;
    private final PDLService pdl;

    public APIOppslagController(AuthContext authContext, PDLService pdl) {
        this.authContext = authContext;
        this.pdl = pdl;
    }

    @GetMapping(path = "me")
    public Søker søker () {
        return new Søker(authContext.getFnr(),pdl.navn());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [tokenUtil=" + authContext + ", pdl=" + pdl + "]";
    }
}
