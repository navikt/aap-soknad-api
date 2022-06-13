package no.nav.aap.api.søknad

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException

object AuthContextExtension {
    fun AuthContext.getFnr(issuer: String = IDPORTEN) = getSubject(issuer)
        ?.let {
            Fødselsnummer(it)
        } ?: throw JwtTokenMissingException("Intet token i context")

    fun AuthContext.getJti(issuer: String = IDPORTEN) =
        getClaim(issuer, "jti") ?: throw JwtTokenMissingException("Intet token i context")
}