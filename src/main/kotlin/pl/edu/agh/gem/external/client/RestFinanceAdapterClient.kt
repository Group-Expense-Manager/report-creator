package pl.edu.agh.gem.external.client

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.resilience4j.retry.annotation.Retry
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import pl.edu.agh.gem.config.FinanceAdapterProperties
import pl.edu.agh.gem.external.dto.finance.ActivitiesResponse
import pl.edu.agh.gem.external.dto.finance.BalancesResponse
import pl.edu.agh.gem.external.dto.finance.SettlementsResponse
import pl.edu.agh.gem.external.dto.finance.toDomain
import pl.edu.agh.gem.headers.HeadersUtils.withAppAcceptType
import pl.edu.agh.gem.internal.client.FinanceAdapterClient
import pl.edu.agh.gem.internal.client.FinanceAdapterClientException
import pl.edu.agh.gem.internal.client.RetryableFinanceAdapterClientException
import pl.edu.agh.gem.internal.model.finance.GroupActivities
import pl.edu.agh.gem.internal.model.finance.GroupBalances
import pl.edu.agh.gem.internal.model.finance.GroupSettlements
import pl.edu.agh.gem.metrics.MeteredClient
import pl.edu.agh.gem.paths.Paths.INTERNAL
import java.io.IOException

@Component
@MeteredClient
class RestFinanceAdapterClient(
    @Qualifier("FinanceAdapterRestTemplate") val restTemplate: RestTemplate,
    val financeAdapterProperties: FinanceAdapterProperties,
) : FinanceAdapterClient {
    @Retry(name = "financeAdapter")
    override fun getActivities(groupId: String): List<GroupActivities> {
        return try {
            restTemplate.exchange(
                resolveActivitiesAddress(groupId),
                GET,
                HttpEntity<Any>(HttpHeaders().withAppAcceptType()),
                ActivitiesResponse::class.java,
            ).body?.toDomain() ?: throw FinanceAdapterClientException("While trying to retrieve activities, received an empty body")
        } catch (ex: Exception) {
            handleException(ex, "retrieve activities")
        }
    }

    @Retry(name = "financeAdapter")
    override fun getBalances(groupId: String): List<GroupBalances> {
        return try {
            restTemplate.exchange(
                resolveBalancesAddress(groupId),
                GET,
                HttpEntity<Any>(HttpHeaders().withAppAcceptType()),
                BalancesResponse::class.java,
            ).body?.toDomain() ?: throw FinanceAdapterClientException("While trying to retrieve balances, received an empty body")
        } catch (ex: Exception) {
            handleException(ex, "retrieve balances")
        }
    }

    @Retry(name = "financeAdapter")
    override fun getSettlements(groupId: String): List<GroupSettlements> {
        return try {
            restTemplate.exchange(
                resolveSettlementsAddress(groupId),
                GET,
                HttpEntity<Any>(HttpHeaders().withAppAcceptType()),
                SettlementsResponse::class.java,
            ).body?.toDomain() ?: throw FinanceAdapterClientException("While trying to retrieve settlements, received an empty body")
        } catch (ex: Exception) {
            handleException(ex, "retrieve settlements")
        }
    }

    private fun <T> handleException(
        ex: Exception,
        action: String,
    ): T {
        when (ex) {
            is HttpClientErrorException -> {
                logger.warn(ex) { "Client-side exception while trying to $action" }
                throw FinanceAdapterClientException(ex.message)
            }
            is HttpServerErrorException, is ResourceAccessException, is IOException -> {
                logger.warn(ex) { "Retryable exception while trying to $action" }
                throw RetryableFinanceAdapterClientException(ex.message)
            }
            else -> {
                logger.warn(ex) { "Unexpected exception while trying to $action" }
                throw FinanceAdapterClientException(ex.message)
            }
        }
    }

    private fun resolveActivitiesAddress(groupId: String) = "${financeAdapterProperties.url}$INTERNAL/activities/groups/$groupId"

    private fun resolveBalancesAddress(groupId: String) = "${financeAdapterProperties.url}$INTERNAL/balances/groups/$groupId"

    private fun resolveSettlementsAddress(groupId: String) = "${financeAdapterProperties.url}$INTERNAL/settlements/groups/$groupId"

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
