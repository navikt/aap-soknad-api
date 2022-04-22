package no.nav.aap.api.søknad

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException

object AuthContextExtension {
    fun AuthContext.getFnr(issuer: String = IDPORTEN) =
        getSubject(issuer)?.let { Fødselsnummer(it) } ?: throw JwtTokenUnauthorizedException("Ikke autentisert")
}