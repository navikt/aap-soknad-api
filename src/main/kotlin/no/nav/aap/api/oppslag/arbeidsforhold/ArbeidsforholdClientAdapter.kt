package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdConfig.Companion.ARBEIDSFORHOLD
import no.nav.aap.api.oppslag.behandler.BehandlerDTO
import no.nav.aap.api.oppslag.organisasjon.OrganisasjonWebClientAdapter
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate.now


@Component
class ArbeidsforholdClientAdapter(
        @Qualifier(ARBEIDSFORHOLD) webClient: WebClient,
        private val orgAdapter: OrganisasjonWebClientAdapter,
        private val cf: ArbeidsforholdConfig) : AbstractWebClientAdapter(webClient, cf) {



    fun arbeidsforhold() =
         webClient
            .get()
             .uri { b -> cf.arbeidsforholdURI(b, now().minus(cf.tidTilbake)) }
            .accept(APPLICATION_JSON)
            .retrieve()
             .bodyToFlux(ArbeidsforholdDTO::class.java)
             .collectList()
             .block()
            ?.map { it.tilArbeidsforhold(orgAdapter.orgNavn(it.arbeidsgiver.organisasjonsnummer)) }.orEmpty()


    override fun toString() = "${javaClass.simpleName} [webClient=$webClient, cfg=$cf]"
}