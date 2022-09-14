package no.nav.aap.api.søknad.arkiv

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.arkiv.ArkivResponse
import no.nav.aap.arkiv.Journalpost
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.Constants.JOARK
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ArkivWebClientAdapter(@Qualifier(JOARK) webClient: WebClient, val cf: ArkivConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    fun opprettJournalpost(journalpost: ArkivJournalpost) =
        webClient.post()
            .uri { b -> b.path(cf.arkivPath).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(journalpost)
            .retrieve()
            .bodyToMono<ArkivResponse>()
            .doOnError { t: Throwable ->
                log.warn("Journalføring feilet", t)
            }
            .doOnSuccess {
                log.trace("Journalført $it OK")
            }
            .block() ?: throw IntegrationException("Null respons fra arkiv")
}