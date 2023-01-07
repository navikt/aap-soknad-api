package no.nav.aap.api.oppslag.graphql

interface GraphQLErrorHandler {
    fun handle(query: String,e: Throwable): Nothing
    companion object {
        const val Ok = "ok"
        const val Unauthorized = "unauthorized"
        const val Unauthenticated = "unauthenticated"
        const val BadRequeest = "bad_request"
        const val NotFound = "not_found"
    }
}