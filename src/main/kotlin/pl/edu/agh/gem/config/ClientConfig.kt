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
    @Qualifier("ExampleRestTemplate")
    fun exampleRestTemplate(
        exampleProperties: ExampleProperties,
        gemRestTemplateFactory: GemRestTemplateFactory,
    ): RestTemplate {
        return gemRestTemplateFactory
            .builder()
            .withReadTimeout(exampleProperties.readTimeout)
            .withConnectTimeout(exampleProperties.connectTimeout)
            .build()
    }
}

@ConfigurationProperties(prefix = "example-client")
data class ExampleProperties(
    val url: String,
    val connectTimeout: Duration,
    val readTimeout: Duration,
)
