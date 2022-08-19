package no.nav.aap.api.søknad.ettersendelse

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.security.token.support.spring.UnprotectedRestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@UnprotectedRestController(value = ["/ettersend"])
@ConditionalOnNotProd
internal class EttersendelseController(private val dittNav: DittNavClient) {

    @GetMapping
    fun ettersend(@RequestParam eventId: UUID, @RequestParam fnr: Fødselsnummer) {
        dittNav.avsluttOppgave(STANDARD, fnr, eventId)
    }
}