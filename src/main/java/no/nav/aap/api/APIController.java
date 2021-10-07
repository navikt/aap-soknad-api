package no.nav.aap.api;

import no.nav.aap.api.pdl.PDLService;
import no.nav.aap.api.util.TokenUtil;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.security.token.support.spring.ProtectedRestController;
import org.springframework.web.bind.annotation.GetMapping;

import static no.nav.aap.api.config.AbstractRestConfig.ISSUER;


@ProtectedRestController(value = "/api", issuer = ISSUER)
public class APIController {

    private final TokenUtil tokenUtil;
    private final PDLService pdl;

    public APIController(TokenUtil tokenUtil, PDLService pdl) {
        this.tokenUtil = tokenUtil;
        this.pdl = pdl;
    }

    @GetMapping(path = "me")
    public String me () {
        return tokenUtil.getSubject() + " "+ pdl.hentNavn() + " er autentisert";
    }

    @GetMapping(path = "open")
    @Unprotected
    public String open () {
        return "open";
    }
}
