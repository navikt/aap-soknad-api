package no.nav.aap.api.søknad;

import no.nav.aap.api.søknad.domain.Søker;
import no.nav.aap.api.søknad.pdl.PDLService;
import no.nav.aap.api.søknad.util.TokenUtil;
import no.nav.security.token.support.spring.ProtectedRestController;
import org.springframework.web.bind.annotation.GetMapping;

import static no.nav.aap.api.søknad.config.AbstractRestConfig.ISSUER;


@ProtectedRestController(value = "/api", issuer = ISSUER)
public class APIController {

    private final TokenUtil tokenUtil;
    private final PDLService pdl;

    public APIController(TokenUtil tokenUtil, PDLService pdl) {
        this.tokenUtil = tokenUtil;
        this.pdl = pdl;
    }

    @GetMapping(path = "me")
    public Søker søker () {
        return new Søker(tokenUtil.getFnr(),pdl.navn());
    }
}
