package pl.edu.agh.gem.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import pl.edu.agh.gem.helper.http.GemRestTemplateFactory
import java.time.Duration

@Configuration
class ClientConfig {
    @Bean
    @Qualifier("GroupManagerRestTemplate")
    fun groupManagerRestTemplate(
        groupManagerProperties: GroupManagerProperties,
        gemRestTemplateFactory: GemRestTemplateFactory,
    ): RestTemplate {
        return gemRestTemplateFactory
            .builder()
            .withReadTimeout(groupManagerProperties.readTimeout)
            .withConnectTimeout(groupManagerProperties.connectTimeout)
            .build()
    }

    @Bean
    @Qualifier("UserDetailsManagerRestTemplate")
    fun userDetailsManagerRestTemplate(
        userDetailsManagerProperties: UserDetailsManagerProperties,
        gemRestTemplateFactory: GemRestTemplateFactory,
    ): RestTemplate {
        return gemRestTemplateFactory
            .builder()
            .withReadTimeout(userDetailsManagerProperties.readTimeout)
            .withConnectTimeout(userDetailsManagerProperties.connectTimeout)
            .build()
    }

    @Bean
    @Qualifier("EmailSenderRestTemplate")
    fun emailSenderRestTemplate(
        emailSenderProperties: EmailSenderProperties,
        gemRestTemplateFactory: GemRestTemplateFactory,
    ): RestTemplate {
        return gemRestTemplateFactory
            .builder()
            .withReadTimeout(emailSenderProperties.readTimeout)
            .withConnectTimeout(emailSenderProperties.connectTimeout)
            .build()
    }

    @Bean
    @Qualifier("FinanceAdapterRestTemplate")
    fun financeAdapterRestTemplate(
        financeAdapterProperties: FinanceAdapterProperties,
        gemRestTemplateFactory: GemRestTemplateFactory,
    ): RestTemplate {
        return gemRestTemplateFactory
            .builder()
            .withReadTimeout(financeAdapterProperties.readTimeout)
            .withConnectTimeout(financeAdapterProperties.connectTimeout)
            .build()
    }

    @Bean
    @Qualifier("AttachmentStoreRestTemplate")
    fun attachmentStoreRestTemplate(
        attachmentStoreProperties: AttachmentStoreProperties,
        gemRestTemplateFactory: GemRestTemplateFactory,
    ): RestTemplate {
        return gemRestTemplateFactory
            .builder()
            .withReadTimeout(attachmentStoreProperties.readTimeout)
            .withConnectTimeout(attachmentStoreProperties.connectTimeout)
            .build()
    }
}

@ConfigurationProperties(prefix = "group-manager")
data class GroupManagerProperties(
    val url: String,
    val connectTimeout: Duration,
    val readTimeout: Duration,
)

@ConfigurationProperties(prefix = "user-details-manager")
data class UserDetailsManagerProperties(
    val url: String,
    val connectTimeout: Duration,
    val readTimeout: Duration,
)

@ConfigurationProperties(prefix = "email-sender")
data class EmailSenderProperties(
    val url: String,
    val connectTimeout: Duration,
    val readTimeout: Duration,
)

@ConfigurationProperties(prefix = "finance-adapter")
data class FinanceAdapterProperties(
    val url: String,
    val connectTimeout: Duration,
    val readTimeout: Duration,
)

@ConfigurationProperties(prefix = "attachment-store")
data class AttachmentStoreProperties(
    val url: String,
    val connectTimeout: Duration,
    val readTimeout: Duration,
)
