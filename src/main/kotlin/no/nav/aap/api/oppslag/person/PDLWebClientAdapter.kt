package no.nav.aap.api.oppslag.person

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.oppslag.graphql.AbstractGraphQLAdapter
import no.nav.aap.api.oppslag.person.PDLMapper.pdlSøkerTilSøker
import no.nav.aap.api.oppslag.person.PDLSøker.PDLForelderBarnRelasjon
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.PDL_SYSTEM
import no.nav.aap.util.Constants.PDL_USER
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
data class WebClients(
        @Qualifier(PDL_USER) val client: WebClient,
        @Qualifier(PDL_USER) val user: GraphQLWebClient,
        @Qualifier(PDL_SYSTEM) val system: GraphQLWebClient)

@Component
class PDLWebClientAdapter(private val clients: WebClients, cfg: PDLConfig, private val ctx: AuthContext) :
    AbstractGraphQLAdapter(clients.client, cfg) {

    override fun ping() :Map<String,String>{
        webClient
            .options()
            .uri(baseUri)
            .accept(APPLICATION_JSON, TEXT_PLAIN)
            .retrieve()
            .toBodilessEntity()
            .block()
        return emptyMap()
    }

    fun søker(medBarn: Boolean = false) =
        with(ctx.getFnr()) {
            query<PDLWrappedSøker>(clients.user, PERSON_QUERY, fnr)?.active?.let {
                pdlSøkerTilSøker(it, this, barnBolk(medBarn,it.forelderBarnRelasjon))
            } ?: throw JwtTokenMissingException()
        }

    fun barn(medBarn:Boolean,forelderBarnRelasjon: List<PDLForelderBarnRelasjon>) =
        if(medBarn) {
            forelderBarnRelasjon.asSequence().mapNotNull { relasjon ->
                relasjon.relatertPersonsIdent?.let { query<PDLBarn>(clients.system, BARN_QUERY, it)
                }
            }
        } else emptySequence()

    fun barnBolk(medBarn:Boolean,forelderBarnRelasjon: List<PDLForelderBarnRelasjon>) =
        try  {
            if(medBarn) {
                val barnIDer  = forelderBarnRelasjon.mapNotNull { it.relatertPersonsIdent }
                if (barnIDer.isNotEmpty()) {
                    log.trace("Slår opp barn $barnIDer")
                    val b = queryBolk<List<String>>(clients.system, BARN_BOLK_QUERY,barnIDer)?.asSequence() ?: emptyList<PDLBarn>().asSequence()
                    log.trace("Slo opp barn $b")
                    throw IllegalStateException("OOPS")

                }
                else {
                    emptyList<PDLBarn>().asSequence()
                }
            } else {
                emptyList<PDLBarn>().asSequence()
            }
        } catch (e: Exception)    {
            log.warn("Fallback grunnet pdl feil", e)
            barn(medBarn,forelderBarnRelasjon)
        }

    override fun toString() =
        "${javaClass.simpleName} [webClient=$webClient,webClients=$clients,authContext=$ctx, cfg=$cfg]"

    companion object {
        private const val BARN_BOLK_QUERY = "query-barnbolk.graphql"
        private const val PERSON_QUERY = "query-person.graphql"
        private const val BARN_QUERY = "query-barn.graphql"
    }
}