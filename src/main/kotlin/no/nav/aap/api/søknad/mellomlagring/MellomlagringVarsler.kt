package no.nav.aap.api.søknad.mellomlagring

import java.time.Duration
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MellomlagringVarsler(private val minside: MinSideClient, private val lager: Mellomlager) {
    val log = getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${buckets.varsel.fixed:60000}", initialDelayString = "\${buckets.varsel.initial:10000}")
    fun scheduleFixedRateWithInitialDelayTask() {
        log.trace("Orphan sjekk : 2 dager skal varsles")
        val data = lager.ikkeOppdatertSiden(Duration.ofDays(2))
        log.trace("2 dager skal varsles: $data")
    }
}