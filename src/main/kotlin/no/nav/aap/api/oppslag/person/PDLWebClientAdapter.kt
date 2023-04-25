package no.nav.aap.api.oppslag.person

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.oppslag.graphql.AbstractGraphQLAdapter
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler.Companion.Ok
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.IDENT
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.IDENTER
import no.nav.aap.api.oppslag.person.PDLBolkBarn.PDLBarn
import no.nav.aap.api.oppslag.person.PDLMapper.beskyttelseBarn
import no.nav.aap.api.oppslag.person.PDLMapper.pdlSøkerTilSøker
import no.nav.aap.api.oppslag.person.PDLSøker.PDLForelderBarnRelasjon
import no.nav.aap.api.søknad.model.Søker.Barn
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.PDL_SYSTEM
import no.nav.aap.util.Constants.PDL_USER
import no.nav.aap.util.StringExtensions.partialMask
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException

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
            .contextCapture()
            .block()
        return emptyMap()
    }

    fun søker(medBarn: Boolean = false) =
        with(ctx.getFnr()) {
            query<PDLWrappedSøker>(clients.user, PERSON_QUERY,  mapOf(IDENT to fnr))?.active?.let {
                pdlSøkerTilSøker(it, this, alleBarn(medBarn,it.forelderBarnRelasjon))
            } ?: throw JwtTokenMissingException()
        }


    fun alleBarn(medBarn:Boolean,forelderBarnRelasjon: List<PDLForelderBarnRelasjon>): Sequence<PDLBarn> =
        if(medBarn) {
            with(forelderBarnRelasjon.mapNotNull { it.relatertPersonsIdent }) {
                if (isNotEmpty()) {
                    with(query<PDLBolkBarn>(clients.system, BARN_BOLK_QUERY, mapOf(IDENTER to this))
                        .partition { it.code == Ok }) {
                        second.forEach {
                            log.warn("Kunne ikke slå opp barn ${it.ident.partialMask()}, kode er ${it.code}")
                        }
                        first.map(::barn).asSequence()
                    }
                 }
                else {
                    emptySequence()
                }
            }
        } else {
            emptySequence()
        }

    private fun barn(b: PDLBolkBarn) =
        with(b) {
            barn.copy(fnr = Fødselsnummer(ident))
        }
    fun harBeskyttetBarn(barn: List<Barn>) = sjekkBeskyttelseBarn(barn.map { it.fnr }.mapNotNull { it?.fnr })


    private fun sjekkBeskyttelseBarn(fnrs: List<String>) =
        runCatching {
            with(fnrs) {
                if (isNotEmpty()) {
                    false
                }
                else {
                    log.trace("Slår opp barn {}", this)
                    beskyttelseBarn(query<PDLBolkBarn>(clients.system, BARN_BOLK_QUERY, mapOf(IDENTER to this)))
                }
            }
        }.getOrElse {
            log.warn("Oppslag beskyttelse feilet",it)
            false
        }

        override fun toString() =
        "${javaClass.simpleName} [webClient=$webClient,webClients=$clients,authContext=$ctx, cfg=$cfg]"

    companion object {
        private const val BARN_BOLK_QUERY = "query-barnbolk.graphql"
        private const val PERSON_QUERY = "query-person.graphql"
    }
}