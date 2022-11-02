package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobListOption.currentDirectory
import com.google.cloud.storage.Storage.BlobListOption.prefix
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class StørelseSjekker(private val lager: Storage) {

    private val log = LoggerUtil.getLogger(javaClass)

    fun størrelse(bøtte: String, fnr: Fødselsnummer) {

        lager.list(bøtte, prefix("${fnr.fnr}/"), currentDirectory()).iterateAll().forEach {
            log.info("(${fnr.fnr})  $bøtte : ${it.name} -> ${it.size}")
        }
    }
}