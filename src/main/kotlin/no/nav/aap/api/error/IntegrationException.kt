package no.nav.aap.api.error

class IntegrationException(msg: String?, cause: Throwable? = null) : RuntimeException(msg, cause)