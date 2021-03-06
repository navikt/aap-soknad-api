package no.nav.aap.api.søknad.joark

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.joark.JoarkResponse
import no.nav.aap.joark.Journalpost
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.Constants.JOARK
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class JoarkWebClientAdapter(@Qualifier(JOARK) webClient: WebClient, val cf: JoarkConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    fun opprettJournalpost(journalpost: Journalpost) =
        webClient.post()
            .uri { b -> b.path(cf.joarkPath).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(journalpost)
            .retrieve()
            .bodyToMono<JoarkResponse>()
            .doOnError { t: Throwable ->
                log.warn("Journalføring feilet", t)
            }
            .doOnSuccess {
                log.trace("Journaført $it OK")
            }
            .block() ?: throw IntegrationException("Null respons fra JOARK")
}