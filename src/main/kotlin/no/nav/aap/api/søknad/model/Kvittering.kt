package no.nav.aap.api.søknad.model

import no.nav.aap.api.mellomlagring.DokumentlagerController.Companion.BASEPATH
import org.aspectj.weaver.tools.cache.SimpleCacheFactory.path
import org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri
import java.net.URI

data class Kvittering private constructor(val uri: URI) {
    constructor(uuid: String) : this(fromCurrentRequestUri().replacePath("$BASEPATH/les/$uuid").build().toUri())
}