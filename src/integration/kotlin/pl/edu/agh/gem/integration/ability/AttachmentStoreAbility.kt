package pl.edu.agh.gem.integration.ability

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK
import pl.edu.agh.gem.headers.HeadersTestUtils.withAppContentType
import pl.edu.agh.gem.integration.BaseIntegrationSpec.Companion.objectMappper
import pl.edu.agh.gem.integration.environment.ProjectConfig.wiremock
import pl.edu.agh.gem.paths.Paths.INTERNAL

private fun uploadGroupAttachmentUrl(groupId: String) = "$INTERNAL/groups/$groupId"

fun stubPostReportUrl(body: Any? = null, groupId: String, userId: String, statusCode: HttpStatus = OK) {
    wiremock.stubFor(
        post(urlPathEqualTo(uploadGroupAttachmentUrl(groupId)))
            .withQueryParam("userId", equalTo(userId))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType()
                    .withBody(objectMappper.writeValueAsString(body)),
            ),
    )
}

fun verifyPostReportUrl(groupId: String, userId: String) {
    wiremock.verify(
        postRequestedFor(urlPathEqualTo(uploadGroupAttachmentUrl(groupId)))
            .withQueryParam("userId", equalTo(userId)),
    )
}
