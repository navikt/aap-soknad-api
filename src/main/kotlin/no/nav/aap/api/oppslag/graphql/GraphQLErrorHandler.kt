package no.nav.aap.api.oppslag.graphql

interface GraphQLErrorHandler {
    fun handle(e: Throwable,query : String): Nothing
    companion object {
        const val Ok = "ok"
        const val Unauthorized = "unauthorized"
        const val Unauthenticated = "unauthenticated"
        const val BadRequest = "bad_request"
        const val NotFound = "not_found"
    }
}