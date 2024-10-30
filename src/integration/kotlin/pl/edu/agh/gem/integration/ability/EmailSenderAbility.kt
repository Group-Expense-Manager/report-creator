package pl.edu.agh.gem.integration.ability

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import org.springframework.http.HttpStatus
import pl.edu.agh.gem.headers.HeadersTestUtils.withAppContentType
import pl.edu.agh.gem.integration.environment.ProjectConfig.wiremock
import pl.edu.agh.gem.paths.Paths.INTERNAL

private fun sendReportNotificationUrl() = "$INTERNAL/report"

fun stubEmailSenderReportNotification(statusCode: HttpStatus = HttpStatus.OK) {
    wiremock.stubFor(
        post(urlMatching(sendReportNotificationUrl()))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType(),
            ),
    )
}
