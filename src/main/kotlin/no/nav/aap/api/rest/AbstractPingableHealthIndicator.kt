package no.nav.aap.api.rest

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator

abstract class AbstractPingableHealthIndicator(private val pingable: Pingable) : HealthIndicator {
    override fun health(): Health {
        return try {
            pingable.ping()
            Health.up().withDetail(pingable.name(), pingable.pingEndpoint()).build()
        } catch (e: Exception) {
            Health.down().withDetail(pingable.name(), pingable.pingEndpoint()).withException(e).build()
        }
    }

    override fun toString() = "${javaClass.simpleName} [pingable=$pingable]"
}