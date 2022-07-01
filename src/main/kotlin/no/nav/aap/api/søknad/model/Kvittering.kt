package no.nav.aap.api.søknad.model

import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentlagerController.Companion.BASEPATH
import org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri
import java.net.URI
import java.util.*

data class Kvittering private constructor(val uri: URI) {
    constructor(uuid: UUID) : this(fromCurrentRequestUri().replacePath("$BASEPATH/les/$uuid").build().toUri())
}