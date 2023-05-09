package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.graphql.client.ClientGraphQlRequest
import org.springframework.graphql.client.GraphQlClient
import org.springframework.graphql.client.GraphQlClientInterceptor
import org.springframework.graphql.client.GraphQlClientInterceptor.Chain
import org.springframework.graphql.client.GraphQlClientInterceptor.SubscriptionChain
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter

abstract class AbstractGraphQLAdapter(client : WebClient, cfg : AbstractRestConfig, val handler : GraphQLErrorHandler = GraphQLDefaultErrorHandler()) :
    AbstractWebClientAdapter(client, cfg) {

    @Retry(name = "graphql")
    protected inline fun <reified T> query(graphQL : GraphQLWebClient, query : String, arg : Map<String, String>) =
        runCatching {
            graphQL.post(query, arg, T::class.java).block().also {
                log.trace("Slo opp {} {}", T::class.java.simpleName, it)
            }
        }.getOrElse {
            handler.handle(it,query)
        }

    @Retry(name = "graphql")
    protected inline fun <reified T> query(graphQL : GraphQLWebClient, query : String, vars : Map<String, List<String>>) =
        runCatching {
            graphQL.flux(query, vars, T::class.java)
                .collectList().block()?.toList().also {
                    log.trace("Slo opp {} {}", T::class.java.simpleName, it)
                } ?: emptyList()
        }.getOrElse {
            handler.handle(it,query)
        }

    @Retry(name = "graphql")
    protected inline fun <reified T> query(graphQL: GraphQlClient, query : Pair<String, String>, vars : Map<String, List<String>>) =
        runCatching {
            (graphQL
                .documentName(query.first)
                .variables(vars)
                .retrieve(query.second)
                .toEntityList(T::class.java)
                .block() ?: emptyList()).also {
                log.trace("Slo opp liste av {} {}", T::class.java.simpleName, it)
            }
        }.getOrElse {
            handler.handle(it,query.first)
        }

    @Retry(name = "graphql")
    protected inline fun <reified T> query(graphQL: GraphQlClient,query : Pair<String, String>, vars : Map<String, String>, info : String) =
        runCatching {
            graphQL
                .documentName(query.first)
                .variables(vars)
                .retrieve(query.second)
                .toEntity(T::class.java)
                .block().also {
                    log.trace("Slo opp {} {}", T::class.java.simpleName, it)
                }
        }.getOrElse { t ->
            log.warn("Query $query feilet. $info", t)
            handler.handle(t, query.first)
        }
}
class GraphQLInterceptor : GraphQlClientInterceptor {

    private val log = LoggerFactory.getLogger(GraphQLInterceptor::class.java)

    override fun intercept(request : ClientGraphQlRequest, chain : Chain) = chain.next(request).also {
        log.trace("Intercepted {} OK", request)
    }

    override fun interceptSubscription(request : ClientGraphQlRequest, chain : SubscriptionChain) = chain.next(request).also {
        log.trace("Intercepted subscription {} OK", request)
    }
}