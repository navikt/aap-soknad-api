package no.nav.aap.api.rest.tokenx

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.util.VersionUtil
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.module.SimpleModule
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse

class TokenXModule : Module() {
    override fun setupModule(context: SetupContext?) {
        val module = SimpleModule()
            .setMixInAnnotation(OAuth2AccessTokenResponse::class.java, IgnoreUnknownMixin::class.java)
        module.setupModule(context)
    }

    override fun getModuleName(): String? {
        return TokenXModule::class.java.simpleName
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private interface IgnoreUnknownMixin

    override fun version(): Version? {
        return VersionUtil.versionFor(TokenXModule::class.java)
    }
}
