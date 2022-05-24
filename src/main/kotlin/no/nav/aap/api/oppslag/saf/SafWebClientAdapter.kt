package no.nav.aap.api.oppslag.saf

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.oppslag.saf.SafConfig.Companion.SAF
import no.nav.aap.api.oppslag.saf.SafConfig.Companion.SAKER_QUERY
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.joark.VariantFormat.ARKIV
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.AuthContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class SafWebClientAdapter(@Qualifier(SAF) client: WebClient,
                          @Qualifier(SAF) private val graphQL: GraphQLWebClient,
                          private val errorHandler: SafErrorHandler,
                          private val ctx: AuthContext,
                          val cf: SafConfig) : AbstractWebClientAdapter(client, cf) {

    fun dokument(journalpostId: String, dokumentInfoId: String) =
        webClient.get()
            .uri { b -> cf.dokUri(b, journalpostId, dokumentInfoId, ARKIV.name) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<ByteArray>()
            .doOnSuccess { log.trace("SAF returnerte ${it.size} bytes") }
            .doOnError { t: Throwable -> log.warn("SAF oppslag feilet", t) }
            .block()

    fun get() = invoke {
        graphQL.post(SAKER_QUERY, mapOf("ident" to ctx.getFnr().fnr, "tema" to "AAP"), MutableMap::class.java).block()
    }

    private fun <T> invoke(oppslag: () -> T): T {
        return try {
            oppslag.invoke()
        }
        catch (e: GraphQLErrorsException) {
            errorHandler.handleError(e)
        }
        catch (e: Exception) {
            log.warn("SAF oppslag feilet med uventet feil", e)
            throw e
        }
    }

}