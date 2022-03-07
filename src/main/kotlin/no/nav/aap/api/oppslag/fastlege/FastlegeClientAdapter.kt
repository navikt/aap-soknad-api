package no.nav.aap.api.oppslag.fastlege

import no.nav.aap.api.felles.Navn
import no.nav.aap.api.oppslag.fastlege.FastlegeConfig.Companion.FASTLEGE
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient


@Component
class FastlegeClientAdapter(
        @Qualifier(FASTLEGE) webClient: WebClient,
        private val cf: FastlegeConfig,
        private val authContext: AuthContext) : AbstractWebClientAdapter(webClient, cf) {

    private val log = LoggerUtil.getLogger(javaClass)
    fun fastlege() = authContext.getSubject()?.let {
        webClient
            .get()
            .uri { b -> b.path(cf.path).build() }
            .accept(APPLICATION_JSON)
            .retrieve()
            .onStatus({ obj: HttpStatus -> obj.isError }) { obj: ClientResponse -> obj.createException() }
            .toEntityList(BehandlerDTO::class.java)
            .block()
            ?.body?.let { it.stream()
                .map(::tilFastlege)
                .findFirst()
            }
    }

    private fun tilFastlege(dto: BehandlerDTO) = Fastlege(Navn(dto.fornavn,dto.mellomnavn,dto.etternavn)) // TODO

    override fun toString() = "${javaClass.simpleName} [webClient=$webClient,authContext=$authContext, cfg=$cf]"
}