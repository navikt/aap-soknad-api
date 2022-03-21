package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdConfig.Companion.ARBEIDSFORHOLD
import no.nav.aap.api.oppslag.krr.KRRWebClientAdapter
import no.nav.aap.api.oppslag.organisasjon.OrganisasjonWebClientAdapter
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.LoggerUtil
import org.slf4j.Logger
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

    private val log: Logger = LoggerUtil.getLogger(javaClass)

    fun arbeidsforhold() =
         webClient
            .get()
             .uri { b -> cf.arbeidsforholdURI(b, now().minus(cf.tidTilbake)) }
            .accept(APPLICATION_JSON)
            .retrieve()
             .bodyToFlux(ArbeidsforholdDTO::class.java)
             .doOnError { t: Throwable -> log.warn("AAREG oppslag areidsforhold feilet", t) }
             .collectList()
             .block()
            ?.map { it.tilArbeidsforhold(orgAdapter.orgNavn(it.arbeidsgiver.organisasjonsnummer)) }.orEmpty()


    override fun toString() = "${javaClass.simpleName} [webClient=$webClient, cfg=$cf]"
}