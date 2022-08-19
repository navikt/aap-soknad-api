package no.nav.aap.api.søknad.ettersendelse

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavRepositories
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.security.token.support.spring.UnprotectedRestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@UnprotectedRestController(["/ettersend"])
@ConditionalOnNotProd
internal class DittNavDevController(private val dittNav: DittNavClient, private val repos: DittNavRepositories) {

    @GetMapping("/avsluttalle")
    fun avslutt(@RequestParam fnr: Fødselsnummer) {
        repos.oppgaver.allNotDone(fnr).forEach { dittNav.avsluttOppgave(STANDARD, fnr, it) }
    }
}