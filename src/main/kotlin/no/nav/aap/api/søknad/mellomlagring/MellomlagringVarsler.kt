package no.nav.aap.api.søknad.mellomlagring

import java.util.*
import java.util.concurrent.TimeUnit.SECONDS
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.MINAAPSTD
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MellomlagringVarsler(private val minside: MinSideClient, private val lager: Mellomlager, val cfg: BucketConfig) {
    val log = getLogger(javaClass)

    @Scheduled(fixedDelayString = "#{'\${buckets.mellom.purring.delay}'}", initialDelay = 10, timeUnit = SECONDS)
    fun scheduleFixedRateWithInitialDelayTask() {
        with(cfg.mellom.purring) {
            log.trace("Orphan sjekk på mellomlagringer eldre enn $alder")
            val gamle = lager.ikkeOppdatertSiden(alder)
            log.trace("${alder.toHoursPart()} timer skal varsles: $gamle")
            if (enabled) {
                gamle.forEach {
                    log.trace("Avslutter ${it.third} for ${it.first} siden opprettet er ${it.second}")
                    minside.avsluttBeskjed(STANDARD,it.first,it.third)
                    minside.opprettBeskjed(it.first,"Dette er en purring", UUID.randomUUID(), MINAAPSTD,true)
                }
            }
        }
    }
}