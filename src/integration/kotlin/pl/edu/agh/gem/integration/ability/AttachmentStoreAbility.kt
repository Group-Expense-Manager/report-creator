package pl.edu.agh.gem.integration.ability

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK
import pl.edu.agh.gem.headers.HeadersTestUtils.withAppContentType
import pl.edu.agh.gem.integration.environment.ProjectConfig.wiremock
import pl.edu.agh.gem.paths.Paths.INTERNAL

private fun createDefaultAttachmentUrl(groupId: String) = "$INTERNAL/groups/$groupId"

fun stubPostReportUrl(body: Any?, groupId: String, statusCode: HttpStatus = OK) {
    wiremock.stubFor(
        post(urlMatching(createDefaultAttachmentUrl(groupId)))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType()
                    .withBody(
                        jacksonObjectMapper().writeValueAsString(body),
                    ),
            ),
    )
}
