package pl.edu.agh.gem.integration.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import pl.edu.agh.gem.external.dto.finance.toDomain
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubGetActivities
import pl.edu.agh.gem.integration.ability.stubGetBalances
import pl.edu.agh.gem.integration.ability.stubGetSettlements
import pl.edu.agh.gem.internal.client.FinanceAdapterClient
import pl.edu.agh.gem.internal.client.FinanceAdapterClientException
import pl.edu.agh.gem.internal.client.RetryableFinanceAdapterClientException
import pl.edu.agh.gem.util.createActivitiesResponse
import pl.edu.agh.gem.util.createBalancesResponse
import pl.edu.agh.gem.util.createSettlementsResponse

class FinanceAdapterClientIT(
    private val financeAdapterClient: FinanceAdapterClient,
) : BaseIntegrationSpec({
    should("retrieve activities successfully") {
        // given
        val activitiesResponse = createActivitiesResponse()
        stubGetActivities(body = activitiesResponse, groupId = GROUP_ID)

        // when
        val result = financeAdapterClient.getActivities(GROUP_ID)

        // then
        result shouldBe activitiesResponse.toDomain()
    }

    should("throw FinanceAdapterClientException on client error for getActivities") {
        // given
        stubGetActivities(groupId = GROUP_ID, statusCode = BAD_REQUEST)

        // when & then
        shouldThrow<FinanceAdapterClientException> {
            financeAdapterClient.getActivities(GROUP_ID)
        }
    }

    should("throw RetryableFinanceAdapterClientException on server error for getActivities") {
        // given
        stubGetActivities(groupId = GROUP_ID, statusCode = INTERNAL_SERVER_ERROR)

        // when & then
        shouldThrow<RetryableFinanceAdapterClientException> {
            financeAdapterClient.getActivities(GROUP_ID)
        }
    }

    // Tests for getBalances
    should("retrieve balances successfully") {
        // given
        val balancesResponse = createBalancesResponse()
        stubGetBalances(body = balancesResponse, groupId = GROUP_ID)

        // when
        val result = financeAdapterClient.getBalances(GROUP_ID)

        // then
        result shouldBe balancesResponse.toDomain()
    }

    should("throw FinanceAdapterClientException on client error for getBalances") {
        // given
        stubGetBalances(groupId = GROUP_ID, statusCode = BAD_REQUEST)

        // when & then
        shouldThrow<FinanceAdapterClientException> {
            financeAdapterClient.getBalances(GROUP_ID)
        }
    }

    should("throw RetryableFinanceAdapterClientException on server error for getBalances") {
        // given
        stubGetBalances(groupId = GROUP_ID, statusCode = INTERNAL_SERVER_ERROR)

        // when & then
        shouldThrow<RetryableFinanceAdapterClientException> {
            financeAdapterClient.getBalances(GROUP_ID)
        }
    }

    // Tests for getSettlements
    should("retrieve settlements successfully") {
        // given
        val settlementsResponse = createSettlementsResponse()
        stubGetSettlements(body = settlementsResponse, groupId = GROUP_ID)

        // when
        val result = financeAdapterClient.getSettlements(GROUP_ID)

        // then
        result shouldBe settlementsResponse.toDomain()
    }

    should("throw FinanceAdapterClientException on client error for getSettlements") {
        // given
        stubGetSettlements(groupId = GROUP_ID, statusCode = BAD_REQUEST)

        // when & then
        shouldThrow<FinanceAdapterClientException> {
            financeAdapterClient.getSettlements(GROUP_ID)
        }
    }

    should("throw RetryableFinanceAdapterClientException on server error for getSettlements") {
        // given
        stubGetSettlements(groupId = GROUP_ID, statusCode = INTERNAL_SERVER_ERROR)

        // when & then
        shouldThrow<RetryableFinanceAdapterClientException> {
            financeAdapterClient.getSettlements(GROUP_ID)
        }
    }
},)
