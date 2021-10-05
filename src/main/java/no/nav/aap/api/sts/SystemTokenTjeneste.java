package no.nav.aap.api.sts;

import static no.nav.aap.api.util.TokenUtil.BEARER;

public interface SystemTokenTjeneste {
    SystemToken getSystemToken();

    default String bearerToken() {
        return BEARER + getSystemToken().getToken();
    }
}
