package no.nav.aap.api.søknad.arkiv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.api.felles.error.RecoverableIntegrationException
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.Constants.JOARK

@Component
class ArkivWebClientAdapter(@Qualifier(JOARK) webClient: WebClient, @Qualifier("${JOARK}ping") pingClient: WebClient, val cf: ArkivConfig) :
    AbstractWebClientAdapter(webClient, cf,pingClient) {

    fun opprettJournalpost(jp: Journalpost) =
        webClient.post()
            .uri { b -> b.path(cf.arkivPath).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(jp)
            .exchangeToMono {
                with(it) {
                    if (statusCode() in listOf(CONFLICT,CREATED)) {
                        bodyToMono(ArkivResponse::class.java)
                    }
                    else {
                        Mono.error(RecoverableIntegrationException("Uventet respons ${statusCode()} fra,${cf.arkivPath}"))
                    }
                }
            }
            .retryWhen(cf.retrySpec(log))
            .doOnError { t: Throwable -> log.warn("Journalføring feilet", t)
                          throw IrrecoverableIntegrationException("Journalføring feilet",cause =t)}
            .contextCapture()
            .block() ?: throw IrrecoverableIntegrationException("Null respons fra arkiv")


    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ArkivResponse(val journalpostId: String,
                             val journalpostferdigstilt: Boolean,
                             val dokumenter: List<DokumentId>) {

        val dokIder = dokumenter.map { it.dokumentInfoId }
        data class DokumentId(val dokumentInfoId: String)
        override fun toString() = "${javaClass.simpleName} [journalpostId=$journalpostId, journalpostferdigstilt=$journalpostferdigstilt, dokumenter= $dokIder]"
    }
}