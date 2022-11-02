package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobListOption.currentDirectory
import com.google.cloud.storage.Storage.BlobListOption.prefix
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.VedleggBucketConfig
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component
import org.springframework.util.unit.DataSize

@Component
class StørelseSjekker(private val lager: Storage) {

    private val log = LoggerUtil.getLogger(javaClass)

    fun størrelse(cfg: VedleggBucketConfig, fnr: Fødselsnummer) =
        lager.list(cfg.navn, prefix("${fnr.fnr}/"), currentDirectory()).iterateAll().sumOf { it.size }.let(DataSize::ofBytes)
}