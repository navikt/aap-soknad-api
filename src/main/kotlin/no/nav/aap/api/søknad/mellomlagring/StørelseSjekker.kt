package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobListOption.currentDirectory
import com.google.cloud.storage.Storage.BlobListOption.prefix
import org.springframework.stereotype.Component
import org.springframework.util.unit.DataSize
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.VedleggBucketConfig
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil

@Component
class StørelseSjekker(private val lager: Storage, private val ctx: AuthContext) {

    val log = LoggerUtil.getLogger(javaClass)
    fun sjekkStørrelse(cfg: VedleggBucketConfig, fnr: Fødselsnummer, dokument: DokumentInfo) =
        with(cfg) {
            val sum = lager.list(navn, prefix("${fnr.fnr}/"), currentDirectory()).iterateAll().sumOf { it.size }
            if (sum + dokument.size > maxsum.toBytes()) {
                log.warn("(${ctx.getFnr()})  Opplasting av dokument med størrelse  ${DataSize.ofBytes(dokument.size)} burde ikke tillates, har allerede lastet opp ${DataSize.ofBytes(sum)}, max pr bruker er er $maxsum")
            }
            else {
                log.trace("Opplasting av dokument med størrelse {} tillates, samlet størrelse av vedlegg i bøtte er {}",
                    DataSize.ofBytes(dokument.size),
                    DataSize.ofBytes(sum))
            }
        }
}