package no.nav.aap.api.søknad.mellomlagring

import java.util.*
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.MINAAPSTD
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MellomlagringVarsler(private val minside: MinSideClient, private val lager: Mellomlager, val cfg: BucketConfig) {
    val log = getLogger(javaClass)

    @Scheduled(fixedDelayString = "#{'\${cfg.mellom.purring.deplay}'}", initialDelay = 1000)
    fun scheduleFixedRateWithInitialDelayTask() {
        with(cfg.mellom.purring) {
            log.trace("Orphan sjekk på $alder")
            val data = lager.ikkeOppdatertSiden(alder)
            log.trace("$alder skal varsles: $data")
            if (enabled) {
                data.forEach {
                    log.trace("Avslutter ${it.third} for ${it.first} siden opprettet er ${it.second}")
                    minside.avsluttBeskjed(STANDARD,it.first,it.third)
                    minside.opprettBeskjed(it.first,"Dette er en purring", UUID.randomUUID(), MINAAPSTD,true)
                }
            }
        }
    }

}