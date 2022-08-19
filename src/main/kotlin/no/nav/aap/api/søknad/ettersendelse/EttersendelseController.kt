package no.nav.aap.api.søknad.ettersendelse

import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@ProtectedRestController(value = ["/ettersend"], issuer = Constants.IDPORTEN)
internal class EttersendelseController(private val dittNav: DittNavClient, private val ctx: AuthContext) {
    private val log = LoggerUtil.getLogger(javaClass)

    @GetMapping
    fun ettersend(@RequestParam eventId: UUID) {
        dittNav.avsluttOppgave(STANDARD, ctx.getFnr(), eventId)
    }
}