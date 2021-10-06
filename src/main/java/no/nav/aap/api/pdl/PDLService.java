package no.nav.aap.api.pdl;

import org.springframework.stereotype.Service;

@Service
public class PDLService {

    private final PDLConnection pdl;

    public PDLService(PDLConnection pdl) {
        this.pdl = pdl;
    }

    public String hentNavn() {
        var n = pdl.hentNavn();
        return n.fornavn() + "," + n.mellomnavn() + "," + n.etternavn();
    }
}
