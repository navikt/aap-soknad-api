package no.nav.aap.api.sts;

import no.nav.aap.api.Pingable;

import static no.nav.aap.api.util.TokenUtil.BEARER;

public interface SystemTokenTjeneste extends Pingable {
    SystemToken getSystemToken();

    default String bearerToken() {
        return BEARER + getSystemToken().getToken();
    }
}
