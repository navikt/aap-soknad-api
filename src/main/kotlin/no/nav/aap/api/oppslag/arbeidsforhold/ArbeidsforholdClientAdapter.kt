package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdConfig.Companion.ARBEIDSFORHOLD
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate


@Component
class ArbeidsforholdClientAdapter(
        @Qualifier(ARBEIDSFORHOLD) webClient: WebClient,
        private val cf: ArbeidsforholdConfig,
        private val authContext: AuthContext) : AbstractWebClientAdapter(webClient, cf) {

    private val log = LoggerUtil.getLogger(javaClass)

    fun arbeidsforhold(): MutableList<Map<*, *>>? {
        log.info("Henter arbeidsforhold")
        return webClient
            .get()
             .uri { b -> cf.arbeidsforholdURI(b, LocalDate.now().minus(cf.tidTilbake)) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatus::isError, ClientResponse::createException)
            .toEntityList(Map::class.java)
            .block()
            ?.body
    }

    override fun toString() = "${javaClass.simpleName} [webClient=$webClient,authContext=$authContext, cfg=$cf]"
}