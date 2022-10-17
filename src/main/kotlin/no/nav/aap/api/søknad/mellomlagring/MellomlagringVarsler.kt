package no.nav.aap.api.søknad.mellomlagring

import java.time.Duration
import java.util.*
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.MINAAPSTD
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.EnvUtil.isDevOrLocal
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MellomlagringVarsler(private val minside: MinSideClient, private val lager: Mellomlager) : EnvironmentAware{
    private lateinit var env: Environment
    val log = getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${buckets.varsel.fixed:60000}", initialDelayString = "\${buckets.varsel.initial:10000}")
    fun scheduleFixedRateWithInitialDelayTask() {
        log.trace("Orphan sjekk : 2 minutter skal varsles")
        val data = lager.ikkeOppdatertSiden(Duration.ofMinutes(2))
        log.trace("2 dager skal varsles: $data")
        if (isDevOrLocal(env)) {
            data.forEach {
                log.trace("Avslutter ${it.third} for ${it.first} siden opprettet er ${it.second}")
                minside.avsluttBeskjed(STANDARD,it.first,it.third)
                minside.opprettBeskjed(it.first,"Dette er en purring", UUID.randomUUID(), MINAAPSTD,true)
            }
        }
    }

    override fun setEnvironment(env: Environment) {
       this.env = env
    }
}