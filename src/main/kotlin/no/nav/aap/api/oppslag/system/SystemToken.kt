package no.nav.aap.api.oppslag.system

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.aap.util.StringExtensions.limit
import no.nav.aap.util.TimeExtensions.toLocalDateTime
import no.nav.security.token.support.core.jwt.JwtToken
import java.time.Duration
import java.time.LocalDateTime


data class SystemToken( @JsonProperty("access_token")val accessToken: JwtToken,
                        @JsonProperty("expires_in") val expiresIn: Long,
                        @JsonProperty("token_type") val tokenType: String,
                        @JsonProperty("scope") val scope: String ){

    fun isExpired(slack: Duration?) = LocalDateTime.now().isAfter(expiration.minus(slack))

    val token = accessToken.tokenAsString
    val expiration = accessToken.jwtTokenClaims.expirationTime.toLocalDateTime()

    override fun toString() = "${javaClass.simpleName} [accessToken=${accessToken.tokenAsString.limit( 12)}, expires=$expiration, scope=$scope, tokenType=$tokenType]"

}