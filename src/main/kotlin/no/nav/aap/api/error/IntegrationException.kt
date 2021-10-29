package no.nav.aap.api.error

class IntegrationException(msg: String? , cause: Throwable? ) :RuntimeException(msg,cause) {
}