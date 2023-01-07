package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import io.github.resilience4j.retry.annotation.Retry
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.web.reactive.function.client.WebClient

abstract class AbstractGraphQLAdapter(client: WebClient, cfg: AbstractRestConfig, val handler: GraphQLErrorHandler = GraphQLDefaultErrorHandler()) : AbstractWebClientAdapter(client, cfg) {

    @Retry(name = "graphql")
    protected inline fun <reified T> query(graphQLClient: GraphQLWebClient, query: String, arg: Map<String,String>) =
        runCatching {
            graphQLClient.post(query, arg, T::class.java).block().also {
                log.trace("Slo opp ${T::class.java.simpleName} $it")
            }
        }.getOrElse {
            handler.handle(query,it)
        }

    @Retry(name = "graphql")
    protected inline fun <reified T> query(graphQLClient: GraphQLWebClient, query: String, vars: Map<String,List<String>>) =
        runCatching {
            graphQLClient.flux(query,vars, T::class.java).collectList().block().also {
               log.trace("Slo opp ${T::class.java.simpleName} $it")
            }
        }.getOrElse {
            handler.handle(query,it)
        }

    companion object {
        const val IDENT = "ident"
        const val IDENTER = "identer"
    }
}