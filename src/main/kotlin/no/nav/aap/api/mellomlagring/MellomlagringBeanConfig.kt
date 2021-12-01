package no.nav.aap.api.mellomlagring

import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.storage.StorageOptions
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.threeten.bp.Duration


@Configuration
class MellomlagringBeanConfig {

    @Bean
    @ConditionalOnGCP
    fun retrySettings(@Value("\${mellomlagring.timeout:3000}") timeoutMs: Long) =
        RetrySettings.newBuilder()
            .setTotalTimeout(Duration.ofMillis(timeoutMs))
            .build()

    @Bean
    @ConditionalOnGCP
    fun storage(retrySettings: RetrySettings) = StorageOptions
        .newBuilder()
        .setRetrySettings(retrySettings)
        .build()
        .service
}