package no.nav.aap.api.rest.sts

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.aap.api.util.TimeUtil.localDateTime
import no.nav.security.token.support.core.jwt.JwtToken
import java.time.Duration
import java.time.LocalDateTime.now

data class SystemToken(@JsonProperty("access_token") private val accessToken: JwtToken,
                       @JsonProperty("expires_in") private val expiresIn: Long,
                       @JsonProperty("token_type") private val  tokenType: String,
                       @JsonProperty("scope") private val scope: String) {

    fun isExpired(slack: Duration) =  now().isAfter(expiration?.minus(slack)) ?: true
    val token = accessToken.tokenAsString
    val expiration = localDateTime(accessToken.jwtTokenClaims.expirationTime)

}