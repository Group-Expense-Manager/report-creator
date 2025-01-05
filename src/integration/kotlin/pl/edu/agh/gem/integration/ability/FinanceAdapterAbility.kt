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

private fun getActivitiesUrl(groupId: String) = "$INTERNAL/activities/groups/$groupId"

private fun getBalancesUrl(groupId: String) = "$INTERNAL/balances/groups/$groupId"

private fun getSettlementsUrl(groupId: String) = "$INTERNAL/settlements/groups/$groupId"

fun stubGetActivities(
    body: Any? = null,
    groupId: String,
    statusCode: HttpStatus = OK,
) {
    wiremock.stubFor(
        get(urlMatching(getActivitiesUrl(groupId)))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType()
                    .withBody(objectMappper.writeValueAsString(body)),
            ),
    )
}

fun stubGetBalances(
    body: Any? = null,
    groupId: String,
    statusCode: HttpStatus = OK,
) {
    wiremock.stubFor(
        get(urlMatching(getBalancesUrl(groupId)))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType()
                    .withBody(objectMappper.writeValueAsString(body)),
            ),
    )
}

fun stubGetSettlements(
    body: Any? = null,
    groupId: String,
    statusCode: HttpStatus = OK,
) {
    wiremock.stubFor(
        get(urlMatching(getSettlementsUrl(groupId)))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType()
                    .withBody(objectMappper.writeValueAsString(body)),
            ),
    )
}
