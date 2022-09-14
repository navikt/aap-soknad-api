package no.nav.aap.api.oppslag.arkiv

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.oppslag.OppslagController
import no.nav.aap.api.oppslag.OppslagController.Companion.DOKUMENT
import no.nav.aap.api.oppslag.OppslagController.Companion.DOKUMENT_PATH
import no.nav.aap.api.oppslag.OppslagController.Companion.OPPSLAG_BASE
import no.nav.aap.api.oppslag.graphql.AbstractGraphQLAdapter
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAF
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAKER_QUERY
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagRelevantDato.ArkivOppslagDatoType.DATO_OPPRETTET
import no.nav.aap.arkiv.VariantFormat.ARKIV
import no.nav.aap.util.AuthContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriBuilder
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.URL
import java.time.LocalDateTime

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
            .doOnSuccess { log.trace("Arkivoppslag returnerte  ${it.size} bytes") }
            .doOnError { t: Throwable -> log.warn("Arkivoppslag feilet", t) }
            .block()

    fun dokumenter(): List<DokumentOversiktInnslag> = oppslag(graphQL.post(SAKER_QUERY, fnr(ctx), ArkivOppslagJournalposter::class.java)
        .block()
        ?.journalposter
        ?.flatMap { it.tilDokumenter() }::orEmpty, "saker")

    fun ArkivOppslagJournalpost.tilDokumenter() = dokumenter.map {
        dok -> DokumentOversiktInnslag(uriFra(journalpostId,dok.dokumentInfoId),dok.tittel,relevanteDatoer.first {
        it.datotype == DATO_OPPRETTET
        }.dato)
    }

    private fun uriFra(journalpostId: String, dokumentId: String) =
        UriComponentsBuilder.newInstance()
        .scheme("https")
        .host("aap-soknad-api.dev.intern.nav.no")
        .path(DOKUMENT_PATH).build(journalpostId,dokumentId).toURL()

    data class DokumentOversiktInnslag(val url: URL, val tittel: String?, val dato: LocalDateTime)

}