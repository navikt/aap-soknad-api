package no.nav.aap.api.søknad.tokenx

import com.nimbusds.oauth2.sdk.token.AccessTokenType
import no.nav.aap.api.søknad.domain.Fødselsnummer
import no.nav.aap.api.søknad.rest.AbstractRestConfig
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.springframework.stereotype.Component
import java.util.*


@Component
class AuthContext(private val ctxHolder: TokenValidationContextHolder) {
    private fun getSubject(issuer: String): String? {
        return getClaim(issuer, "pid")
    }

    val subject: String?
        get() = getSubject(AbstractRestConfig.ISSUER)

    fun getClaim(issuer: String, claim: String?) = claimSet(issuer)?.getStringClaim(claim)

    private fun claimSet(issuer: String = AbstractRestConfig.ISSUER) = context()?.getClaims(issuer)

    private fun context(): TokenValidationContext? {
        return Optional.ofNullable(ctxHolder.tokenValidationContext)
            .orElse(null)
    }

    @JvmOverloads
    fun erAutentisert(issuer: String = AbstractRestConfig.ISSUER): Boolean {
        return getToken(issuer) != null
    }

    val token: String?
        get() = getToken(AbstractRestConfig.ISSUER)

    private fun getToken(issuer: String) = context()?.getJwtToken(issuer)?.tokenAsString

    val jWTToken: JwtToken
        get() = getJWTToken(AbstractRestConfig.ISSUER)

    private fun getJWTToken(issuer: String): JwtToken {
        return ctxHolder.tokenValidationContext.getJwtToken(issuer)
    }

    override fun toString() = "${javaClass.simpleName} [ctxHolder=$ctxHolder]"

    val fnr: Fødselsnummer
        get() = Optional.ofNullable(subject)
            .map { fnr: String? ->
                Fødselsnummer(
                    fnr!!
                )
            }.orElseThrow()

    companion object {
        private val BEARER = AccessTokenType.BEARER.value + " "
        @JvmStatic
        fun bearerToken(token: String): String {
            return BEARER + token
        }
    }
}
