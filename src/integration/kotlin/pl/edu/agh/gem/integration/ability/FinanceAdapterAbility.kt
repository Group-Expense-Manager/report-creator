package pl.edu.agh.gem.integration.ability

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK
import pl.edu.agh.gem.headers.HeadersTestUtils.withAppContentType
import pl.edu.agh.gem.integration.environment.ProjectConfig.wiremock
import pl.edu.agh.gem.paths.Paths.INTERNAL

private fun getActivitiesUrl(groupId: String) = "$INTERNAL/expenses/groups/$groupId"
private fun getBalancesUrl(groupId: String) = "$INTERNAL/balances/groups/$groupId"
private fun getSettlementsUrl(groupId: String) = "$INTERNAL/settlements/groups/$groupId"

fun stubGetActivities(body: Any?, groupId: String, statusCode: HttpStatus = OK) {
    wiremock.stubFor(
        get(urlMatching(getActivitiesUrl(groupId)))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType()
                    .withBody(jacksonObjectMapper().registerModules(JavaTimeModule()).writeValueAsString(body)),
            ),
    )
}

fun stubGetBalances(body: Any?, groupId: String, statusCode: HttpStatus = OK) {
    wiremock.stubFor(
        get(urlMatching(getBalancesUrl(groupId)))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType()
                    .withBody(jacksonObjectMapper().registerModules(JavaTimeModule()).writeValueAsString(body)),
            ),
    )
}

fun stubGetSettlements(body: Any?, groupId: String, statusCode: HttpStatus = OK) {
    wiremock.stubFor(
        get(urlMatching(getSettlementsUrl(groupId)))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType()
                    .withBody(jacksonObjectMapper().registerModules(JavaTimeModule()).writeValueAsString(body)),
            ),
    )
}
