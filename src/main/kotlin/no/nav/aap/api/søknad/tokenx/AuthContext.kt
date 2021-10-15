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

    fun getClaim(issuer: String, claim: String?): String? {
        return Optional.ofNullable(claimSet(issuer))
            .map { c: JwtTokenClaims -> c.getStringClaim(claim) }
            .orElse(null)
    }

    private fun claimSet(issuer: String = AbstractRestConfig.ISSUER): JwtTokenClaims? {
        return Optional.ofNullable(context())
            .map { s: TokenValidationContext -> s.getClaims(issuer) }
            .orElse(null)
    }

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

    private fun getToken(issuer: String): String? {
        return Optional.ofNullable(context())
            .map { c: TokenValidationContext -> c.getJwtToken(issuer) }
            .filter { obj: JwtToken? -> Objects.nonNull(obj) }
            .map { obj: JwtToken -> obj.tokenAsString }
            .orElse(null)
    }

    val jWTToken: JwtToken
        get() = getJWTToken(AbstractRestConfig.ISSUER)

    private fun getJWTToken(issuer: String): JwtToken {
        return ctxHolder.tokenValidationContext.getJwtToken(issuer)
    }

    override fun toString(): String {
        return javaClass.simpleName + " [ctxHolder=" + ctxHolder + "]"
    }

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