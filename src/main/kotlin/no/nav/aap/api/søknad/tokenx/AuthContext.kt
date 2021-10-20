package no.nav.aap.api.søknad.tokenx

import com.nimbusds.oauth2.sdk.token.AccessTokenType
import no.nav.aap.api.søknad.domain.Fødselsnummer
import no.nav.aap.api.søknad.rest.AbstractRestConfig.Companion.ISSUER
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.stereotype.Component


@Component
class AuthContext(private val ctxHolder: TokenValidationContextHolder) {
    fun getSubject(issuer: String = ISSUER) = getClaim(issuer, "pid")
    fun getFnr(issuer: String = ISSUER): Fødselsnummer  = getSubject(issuer)?.let{ Fødselsnummer(it) } ?: throw RuntimeException()
    fun getJWTToken(issuer: String = ISSUER): JwtToken =  context.getJwtToken(issuer)
    fun getClaim(issuer: String, claim: String?) = claimSet(issuer)?.getStringClaim(claim)
    fun erAutentisert(issuer: String = ISSUER) =getToken(issuer) != null
    private val context  = ctxHolder.tokenValidationContext
    private fun getToken(issuer: String) = context?.getJwtToken(issuer)?.tokenAsString
    private fun claimSet(issuer: String) = context?.getClaims(issuer)
    override fun toString() = "${javaClass.simpleName} [ctxHolder=$ctxHolder]"

    companion object {
        private val BEARER = AccessTokenType.BEARER.value + " "
        @JvmStatic
        fun bearerToken(token: String): String {
            return BEARER + token
        }
    }
}
