package no.nav.aap.api.oppslag.arkiv

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.oppslag.graphql.AbstractGraphQLAdapter
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAF
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAKER_QUERY
import no.nav.aap.api.søknad.arkiv.ArkivJournalpost.Dokument.DokumentVariant.VariantFormat.ARKIV
import no.nav.aap.util.AuthContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ArkivOppslagWebClientAdapter(
    @Qualifier(SAF) client: WebClient,
    @Qualifier(SAF) private val graphQL: GraphQLWebClient,
    errorHandler: GraphQLErrorHandler,
    private val ctx: AuthContext,
    val cf: ArkivOppslagConfig) : AbstractGraphQLAdapter(client, cf, errorHandler) {

    fun dokument(journalpostId: String, dokumentInfoId: String) =
        webClient.get()
            .uri { b -> cf.dokUri(b, journalpostId, dokumentInfoId, ARKIV.name) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<ByteArray>()
            .doOnSuccess { log.trace("Arkiv oppslag returnerte ${it.size} bytes") }
            .doOnError { t: Throwable -> log.warn("Arkiv oppslag feilet", t) }
            .block()

    fun saker() = oppslag({ graphQL.post(SAKER_QUERY, fnr(ctx),ArkivOppslagJournalposter::class.java).block() }, "saker")

}