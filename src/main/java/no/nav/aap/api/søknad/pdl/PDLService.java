package no.nav.aap.api.søknad.pdl;

import no.nav.aap.api.søknad.domain.Navn;
import org.springframework.stereotype.Service;

@Service
public class PDLService implements PdlOperations {

    private final PDLWebClientAdapter pdl;

    public PDLService(PDLWebClientAdapter pdl) {
        this.pdl = pdl;
    }
    @Override
    public Navn navn() {
        var n =  pdl.navn();
        return new Navn(n.fornavn(), n.mellomnavn(), n.etternavn());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [pdl=" + pdl + "]";
    }
}
