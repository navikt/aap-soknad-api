package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import io.github.resilience4j.retry.annotation.Retry
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter

abstract class AbstractGraphQLAdapter(client: WebClient, cfg: AbstractRestConfig, val handler: GraphQLErrorHandler = GraphQLDefaultErrorHandler()) : AbstractWebClientAdapter(client, cfg) {

    @Retry(name = "graphql")
    protected inline fun <reified T> query(graphQL: GraphQLWebClient, query: String, arg: Map<String,String>) =
        runCatching {
            graphQL.post(query, arg, T::class.java).block().also {
                log.trace("Slo opp ${T::class.java.simpleName} $it")
            }
        }.getOrElse {
            handler.handle(it)
        }

    @Retry(name = "graphql")
    protected inline fun <reified T> query(graphQL: GraphQLWebClient, query: String, vars: Map<String,List<String>>) =
        runCatching {
            graphQL.flux(query,vars, T::class.java)
                .collectList().block()?.toList().also {
                    log.trace("Slo opp ${T::class.java.simpleName} $it")
                }  ?: emptyList()
        }.getOrElse {
            handler.handle(it)
        }
}