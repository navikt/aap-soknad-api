package no.nav.aap.api.mellomlagring

import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.threeten.bp.Duration


@Configuration
@ConditionalOnGCP
class MellomlagringBeanConfig {

    @Bean
    fun retrySettings(@Value("\${mellomlagring.timeout:3000}") timeoutMs: Long): RetrySettings =
        RetrySettings.newBuilder()
            .setTotalTimeout(Duration.ofMillis(timeoutMs))
            .build()

    @Bean
    fun storage(retrySettings: RetrySettings): Storage = StorageOptions
        .newBuilder()
        .setRetrySettings(retrySettings)
        .build()
        .service
}