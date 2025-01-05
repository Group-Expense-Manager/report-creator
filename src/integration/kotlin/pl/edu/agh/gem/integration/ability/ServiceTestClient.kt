package pl.edu.agh.gem.integration.ability

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.servlet.client.MockMvcWebTestClient.bindToApplicationContext
import org.springframework.web.context.WebApplicationContext
import pl.edu.agh.gem.external.dto.report.GenerateReportRequest
import pl.edu.agh.gem.headers.HeadersUtils.withAppContentType
import pl.edu.agh.gem.headers.HeadersUtils.withValidatedUser
import pl.edu.agh.gem.integration.BaseIntegrationSpec.Companion.objectMappper
import pl.edu.agh.gem.paths.Paths.EXTERNAL
import pl.edu.agh.gem.security.GemUser
import java.net.URI

@Component
@Lazy
class ServiceTestClient(applicationContext: WebApplicationContext) {
    private val webClient =
        bindToApplicationContext(applicationContext)
            .configureClient()
            .build()

    fun generateReport(
        gemUser: GemUser?,
        groupId: String,
        generateReportRequest: GenerateReportRequest,
    ): ResponseSpec {
        return webClient.post()
            .uri(URI("$EXTERNAL/generate/groups/$groupId"))
            .headers {
                if (gemUser != null) it.withValidatedUser(gemUser)
                it.withAppContentType()
            }
            .bodyValue(objectMappper.writeValueAsString(generateReportRequest))
            .exchange()
    }
}
