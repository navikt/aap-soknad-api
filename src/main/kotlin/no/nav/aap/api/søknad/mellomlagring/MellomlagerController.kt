package no.nav.aap.api.søknad.mellomlagring

import io.micrometer.observation.annotation.Observed
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController

@ProtectedRestController(value = ["buckets"], issuer = IDPORTEN)
@Observed
internal class MellomlagerController(private val mellomlager: Mellomlager, private val dokumentlager: Dokumentlager ) {

    @PostMapping("/lagre/{type}")
    @ResponseStatus(CREATED)
    fun lagre(@PathVariable type: SkjemaType, @RequestBody data: String) = mellomlager.lagre(data, type)

    @GetMapping("/les/{type}")
    fun les(@PathVariable type: SkjemaType) = mellomlager.les(type)

    @DeleteMapping("/slett/{type}")
    @ResponseStatus(NO_CONTENT)
    fun slett(@PathVariable type: SkjemaType) = mellomlager.slett(type).also { dokumentlager.slettAlleDokumenter() }

}