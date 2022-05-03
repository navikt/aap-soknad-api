package no.nav.aap.api.søknad.model

import no.nav.aap.api.mellomlagring.DokumentlagerController.Companion.BASEPATH
import org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri
import java.net.URI

data class Kvittering private constructor(val uri: URI) {
    constructor(path: String) : this(fromCurrentRequestUri().replacePath("$BASEPATH/les/$path").build().toUri())
}