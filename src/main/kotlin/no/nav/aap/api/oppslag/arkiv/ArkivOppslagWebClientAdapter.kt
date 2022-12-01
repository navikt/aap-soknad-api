package no.nav.aap.api.oppslag.arkiv

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import java.time.LocalDateTime
import java.util.*
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.DOKUMENTER_QUERY
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAF
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagDokumentInfo.ArkivOppslagDokumentVariant.ArkivOppslagDokumentFiltype.PDF
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagDokumentInfo.ArkivOppslagDokumentVariant.ArkivOppslagDokumentVariantFormat.ARKIV
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagJournalpostType
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagJournalpostType.I
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagJournalpostType.U
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagRelevantDato.ArkivOppslagDatoType.DATO_OPPRETTET
import no.nav.aap.api.oppslag.graphql.AbstractGraphQLAdapter
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
        private val ctx: AuthContext,
        private val mapper: ArkivOppslagMapper,
        val cf: ArkivOppslagConfig) : AbstractGraphQLAdapter(client, cf) {

    fun dokument(journalpostId: String, dokumentInfoId: String) =
        webClient.get()
            .uri(cf.dokUri(), journalpostId, dokumentInfoId,ARKIV.name)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<ByteArray>()
            .doOnSuccess { log.trace("Arkivoppslag returnerte  ${it.size} bytes") }
            .doOnError { t: Throwable -> log.warn("Arkivoppslag feilet", t) }
            .block() ?: throw IntegrationException("Null response fra arkiv")

    fun dokumenter() = query()
        ?.filter { it.journalposttype in listOf(I, U) }
        ?.flatMap { mapper.tilDokumenter(it) }
        .orEmpty()

    fun søknadDokumentId(journalPostId: String) = query()
        ?.firstOrNull { it.journalpostId == journalPostId }
        ?.dokumenter?.firstOrNull()?.dokumentInfoId   // Søknaden er alltid  første elementet

    private fun query() = query<ArkivOppslagJournalposter>(graphQL, DOKUMENTER_QUERY, ctx.getFnr().fnr)
        ?.journalposter.also {
            log.trace("GraphQL oppslag returnerte $it")
        }
}

@Component
class ArkivOppslagMapper {
    fun tilDokumenter(journalpost: ArkivOppslagJournalpost) =
        with(journalpost) {
            dokumenter.filter { v ->
                v.dokumentvarianter.any {
                    with(it) {
                        filtype == PDF && brukerHarTilgang && ARKIV == variantformat
                    }
                }
            }.map { dok ->
                DokumentOversiktInnslag(
                        journalpostId, dok.dokumentInfoId,
                        dok.tittel,
                        journalposttype,
                        eksternReferanseId,
                        relevanteDatoer.first {
                            it.datotype == DATO_OPPRETTET
                        }.dato)
            }.sortedByDescending { it.dato }
        }

    data class DokumentOversiktInnslag(val journalpostId: String,
                                       val dokumentId: String,
                                       val tittel: String?,
                                       val type: ArkivOppslagJournalpostType,
                                       val innsendingId: String?,
                                       val dato: LocalDateTime)

}