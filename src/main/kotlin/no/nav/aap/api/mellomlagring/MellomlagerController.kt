package no.nav.aap.api.mellomlagring

import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus

@ProtectedRestController(value = ["buckets"], issuer = IDPORTEN)
internal class MellomlagerController(private val lager: Mellomlager,
                                     private val dittnav: DittNavClient,
                                     private val ctx: AuthContext) {

    @PostMapping("/lagre/{type}")
    @ResponseStatus(CREATED)
    fun lagre(@PathVariable type: SkjemaType, @RequestBody data: String) =
        lager.lagre(ctx.getFnr(), type, data)

    @GetMapping("/les/{type}")
    fun les(@PathVariable type: SkjemaType) =
        lager.les(ctx.getFnr(), type)?.let { ok(it) } ?: notFound().build()

    @DeleteMapping("/slett/{type}")
    fun slett(@PathVariable type: SkjemaType): ResponseEntity<Void> {
        return if (lager.slett(ctx.getFnr(), type)) {
            dittnav.fjernOgAvsluttMellomlagring()
            noContent().build()
        }
        else {
            notFound().build()
        }
    }
}