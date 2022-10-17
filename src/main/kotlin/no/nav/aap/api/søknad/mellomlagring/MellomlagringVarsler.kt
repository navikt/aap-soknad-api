package no.nav.aap.api.søknad.mellomlagring

import java.time.Duration
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MellomlagringVarsler(private val minside: MinSideClient, private val lager: Mellomlager) {
    val log = getLogger(javaClass)

    @Scheduled(fixedDelay = 60*1000, initialDelay = 1000)
    fun scheduleFixedRateWithInitialDelayTask() {
        log.trace("Orphan sjekk : Ikke oppdatert på 10 dager")
        val data = lager.ikkeOppdatertSiden(Duration.ofDays(10))
        log.trace("Ikke oppdatert på 10 dager er: $data")
    }
}