package no.nav.aap.api.oppslag.arkiv

import java.time.LocalDateTime
import java.util.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.graphql.client.GraphQlClient
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.api.felles.graphql.AbstractGraphQLAdapter
import no.nav.aap.api.felles.graphql.GraphQLExtensions.IDENT
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAF
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagDokumentInfo.ArkivOppslagDokumentVariant
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagDokumentInfo.ArkivOppslagDokumentVariant.ArkivOppslagDokumentVariantFormat.ARKIV
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagJournalpostType
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagJournalpostType.I
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagJournalpostType.U
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagRelevantDato.ArkivOppslagDatoType.DATO_OPPRETTET
import no.nav.aap.util.AuthContext

@Component
class ArkivOppslagWebClientAdapter(
    @Qualifier(SAF) webClient: WebClient,
    @Qualifier(SAF) private val graphQLClient: GraphQlClient,
    private val ctx: AuthContext,
    private val mapper: ArkivOppslagMapper,
    val cf: ArkivOppslagConfig) : AbstractGraphQLAdapter(webClient,cf) {

    fun dokument(journalpostId: String, dokumentInfoId: String) =
        webClient.get()
            .uri(cf.dokUri(), journalpostId, dokumentInfoId,ARKIV.name)
            .accept(APPLICATION_JSON)
            .retrieve()
            .onStatus({ UNAUTHORIZED == it }, { Mono.empty<Throwable>().also { log.trace("Dokument $journalpostId/$dokumentInfoId kan ikke slås opp") } })
            .bodyToMono<ByteArray>()
            .retryWhen(cf.retrySpec(log))
            .doOnSuccess { log.trace("Arkivoppslag returnerte  ${it.size} bytes") }
            .contextCapture()
            .block() ?: throw IrrecoverableIntegrationException("Null response fra arkiv for  $journalpostId/$dokumentInfoId ")

    fun dokumenter() = query()
        ?.filter { it.journalposttype in listOf(I, U) }
        ?.flatMap { mapper.tilDokumenter(it) }
        .orEmpty()
    fun søknadDokumentId(journalPostId: String) = query()
        ?.firstOrNull { it.journalpostId == journalPostId }
        ?.dokumenter?.firstOrNull()?.dokumentInfoId   // Søknaden er alltid  første elementet

    private fun query() = query<ArkivOppslagJournalposter>(graphQLClient,  DOKUMENTER_QUERY, ctx.toIdent(),"Fnr: ${ctx.getFnr()}")?.journalposter


    private fun AuthContext.toIdent() = mapOf(IDENT to getFnr().fnr)

    companion object {
        private val DOKUMENTER_QUERY = Pair("query-dokumenter","dokumentoversiktSelvbetjening")
    }
}

@Component
class ArkivOppslagMapper {
    fun tilDokumenter(journalpost: ArkivOppslagJournalpost) =
        with(journalpost) {
            dokumenter.filter { v ->
                v.dokumentvarianter.any(ArkivOppslagDokumentVariant::kanVises)
            }.map { dok ->
                DokumentOversiktInnslag(
                        journalpostId, dok.dokumentInfoId,
                        dok.tittel,
                        journalposttype,
                        eksternReferanseId,
                        relevanteDatoer.first { it.datotype == DATO_OPPRETTET }.dato)
            }.sortedByDescending { it.dato }
        }

    data class DokumentOversiktInnslag(val journalpostId: String,
                                       val dokumentId: String,
                                       val tittel: String?,
                                       val type: ArkivOppslagJournalpostType,
                                       val innsendingId: String?,
                                       val dato: LocalDateTime)

}