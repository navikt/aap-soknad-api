package no.nav.aap.api.mellomlagring

import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.SkjemaType
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@ProtectedRestController(value = ["buckets"], issuer = IDPORTEN)
class MellomlagringController(private val lager: Mellomlagring, private val authCtx: AuthContext) {

    @PostMapping("/lagre/{type}")
    fun lagre(@PathVariable type: SkjemaType, @RequestBody data: String): ResponseEntity<String> {
        lager.lagre(authCtx.getFnr(), type, data)
        return ResponseEntity<String>(data, CREATED)
    }

    @GetMapping("/les/{type}")
    fun les(@PathVariable type: SkjemaType) = lager.les(authCtx.getFnr(), type)

    @DeleteMapping("/slett/{type}")
    fun slett(@PathVariable type: SkjemaType): ResponseEntity<Void> {
        lager.slett(authCtx.getFnr(), type)
        return ResponseEntity<Void>(NO_CONTENT)
    }
}