package no.nav.aap.api.oppslag.saf

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.oppslag.graphql.AbstractGraphQLAdapter
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler
import no.nav.aap.api.oppslag.saf.SafConfig.Companion.SAF
import no.nav.aap.api.oppslag.saf.SafConfig.Companion.SAKER_QUERY
import no.nav.aap.joark.VariantFormat.ARKIV
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.Constants.TEMA
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class SafWebClientAdapter(@Qualifier(SAF) client: WebClient,
                          @Qualifier(SAF) private val graphQL: GraphQLWebClient,
                          errorHandler: GraphQLErrorHandler,
                          private val ctx: AuthContext,
                          val cf: SafConfig) : AbstractGraphQLAdapter(client, cf, errorHandler) {

    fun dokument(journalpostId: String, dokumentInfoId: String) =
        webClient.get()
            .uri { b -> cf.dokUri(b, journalpostId, dokumentInfoId, ARKIV.name) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<ByteArray>()
            .doOnSuccess { log.trace("SAF returnerte ${it.size} bytes") }
            .doOnError { t: Throwable -> log.warn("SAF oppslag feilet", t) }
            .block()

    fun sakerMetadata() = oppslag({
        graphQL.post(SAKER_QUERY, mapOf(IDENT to ctx.getFnr().fnr, TEMA to AAP), MutableMap::class.java).block()
    }, "saker")

}