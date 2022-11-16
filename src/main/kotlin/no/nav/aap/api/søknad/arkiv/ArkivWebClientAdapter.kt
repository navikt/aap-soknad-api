package no.nav.aap.api.søknad.arkiv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.Constants.JOARK
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ArkivWebClientAdapter(@Qualifier(JOARK) webClient: WebClient, @Qualifier("${JOARK}ping") pingClient: WebClient, val cf: ArkivConfig) :
    AbstractWebClientAdapter(webClient, cf,pingClient) {

    fun opprettJournalpost(journalpost: Journalpost) =
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
                log.info("Journalført ${journalpost.dokumenter} med tittel ${journalpost.tittel}, OK respons er $it")
            }
            .block() ?: throw IntegrationException("Null respons fra arkiv")

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ArkivResponse(val journalpostId: String,
                             val journalpostferdigstilt: Boolean,
                             val dokumenter: List<DokumentId>) {

        val dokIder = dokumenter.map { it.dokumentInfoId }

        data class DokumentId(val dokumentInfoId: String)
        override fun toString() = "${javaClass.simpleName} [journalpostId=$journalpostId, journalpostferdigstilt=$journalpostferdigstilt, dokumenter= $dokIder]"
    }
}