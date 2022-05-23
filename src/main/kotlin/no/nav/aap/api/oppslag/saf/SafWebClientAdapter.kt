package no.nav.aap.api.oppslag.saf

import no.nav.aap.api.oppslag.saf.SafConfig.Companion.SAF
import no.nav.aap.joark.VariantFormat.ARKIV
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class SafWebClientAdapter(@Qualifier(SAF) client: WebClient, val cf: SafConfig) : AbstractWebClientAdapter(client, cf) {

    fun dokument(journalpostId: String, dokumentInfoId: String) =
        webClient.get()
            .uri { b -> cf.dokUri(b, journalpostId, dokumentInfoId, ARKIV.name) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<ByteArray>()
            .doOnSuccess { log.trace("SAF er $it") }
            .doOnError { t: Throwable -> log.warn("SAF oppslag feilet", t) }
            .block()
}