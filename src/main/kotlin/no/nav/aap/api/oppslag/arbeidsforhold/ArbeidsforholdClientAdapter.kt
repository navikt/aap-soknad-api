package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdConfig.Companion.ARBEIDSFORHOLD
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.AuthContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient


@Component
class ArbeidsforholdClientAdapter(
        @Qualifier(ARBEIDSFORHOLD) webClient: WebClient,
        private val cf: ArbeidsforholdConfig,
        private val authContext: AuthContext) : AbstractWebClientAdapter(webClient, cf) {


    override fun toString() = "${javaClass.simpleName} [webClient=$webClient,authContext=$authContext, cfg=$cf]"
}