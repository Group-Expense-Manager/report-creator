package pl.edu.agh.gem.integration.ability

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK
import pl.edu.agh.gem.headers.HeadersTestUtils.withAppContentType
import pl.edu.agh.gem.integration.BaseIntegrationSpec.Companion.objectMappper
import pl.edu.agh.gem.integration.environment.ProjectConfig.wiremock
import pl.edu.agh.gem.paths.Paths.INTERNAL

private fun createUserGroupsUrl(userId: String) = "$INTERNAL/groups/users/$userId"

fun stubGroupManagerUserGroups(body: Any? = null, userId: String, statusCode: HttpStatus = OK) {
    wiremock.stubFor(
        get(urlMatching(createUserGroupsUrl(userId)))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType()
                    .withBody(
                        objectMappper.writeValueAsString(body),
                    ),
            ),
    )
}

private fun createGroupDataUrl(groupId: String) = "$INTERNAL/groups/$groupId"

fun stubGroupManagerGroupDetails(body: Any? = null, groupId: String, statusCode: HttpStatus = OK) {
    wiremock.stubFor(
        get(urlMatching(createGroupDataUrl(groupId)))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType()
                    .withBody(
                        objectMappper.writeValueAsString(body),
                    ),
            ),
    )
}
