package no.nav.aap.api.innsending;

import no.nav.aap.api.søknad.domain.Kvittering;
import no.nav.aap.api.søknad.domain.UtenlandsSøknad;
import no.nav.aap.api.søknad.util.TokenUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;


//@ProtectedRestController(value = "/api/innsending", issuer = ISSUER)
public class SøknadInnsendingController {

    private final TokenUtil tokenUtil;

    public SøknadInnsendingController(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    @PostMapping("/utland")
    public Kvittering utland(@Valid @RequestBody UtenlandsSøknad søknad) {
        return new Kvittering();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [tokenUtil=" + tokenUtil + "]";
    }
}
