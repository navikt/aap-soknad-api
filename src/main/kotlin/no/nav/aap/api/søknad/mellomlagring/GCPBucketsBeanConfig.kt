package no.nav.aap.api.søknad.mellomlagring

import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.storage.StorageOptions
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.threeten.bp.Duration.ofMillis

@Configuration
@ConditionalOnGCP
class GCPBucketsBeanConfig(val cfg: BucketsConfig) {

    @Bean
    fun retrySettings() =
        RetrySettings.newBuilder()
            .setTotalTimeout(ofMillis(cfg.timeoutMs))
            .build()

    @Bean
    fun storage(retrySettings: RetrySettings) = StorageOptions
        .newBuilder()
        .setRetrySettings(retrySettings)
        .build()
        .service

}