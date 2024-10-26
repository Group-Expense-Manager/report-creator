package pl.edu.agh.gem.external.client

import io.github.resilience4j.retry.annotation.Retry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
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
import pl.edu.agh.gem.internal.model.finance.Activity
import pl.edu.agh.gem.internal.model.finance.Balances
import pl.edu.agh.gem.internal.model.finance.Settlements
import pl.edu.agh.gem.paths.Paths.INTERNAL

@Component
class RestFinanceAdapterClient(
    @Qualifier("FinanceAdapterRestTemplate") val restTemplate: RestTemplate,
    val financeAdapterProperties: FinanceAdapterProperties,
) : FinanceAdapterClient {

    @Retry(name = "financeAdapter")
    override fun getActivities(groupId: String): List<Activity> {
        return try {
            restTemplate.exchange(
                resolveActivitiesAddress(groupId),
                GET,
                HttpEntity<Any>(HttpHeaders().withAppAcceptType()),
                ActivitiesResponse::class.java,
            ).body?.toDomain() ?: throw FinanceAdapterClientException("While trying to retrieve activities we receive empty body")
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to retrieve activities" }
            throw FinanceAdapterClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to retrieve activities" }
            throw RetryableFinanceAdapterClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to retrieve activities" }
            throw FinanceAdapterClientException(ex.message)
        }
    }
    
    @Retry(name = "financeAdapter")
    override fun getBalances(groupId: String): List<Balances> {
        return try {
            restTemplate.exchange(
                    resolveBalancesAddress(groupId),
                    GET,
                    HttpEntity<Any>(HttpHeaders().withAppAcceptType()),
                    BalancesResponse::class.java,
            ).body?.toDomain() ?: throw FinanceAdapterClientException("While trying to retrieve balances we receive empty body")
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to retrieve balances" }
            throw FinanceAdapterClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to retrieve balances" }
            throw RetryableFinanceAdapterClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to retrieve balances" }
            throw FinanceAdapterClientException(ex.message)
        }
    }

    @Retry(name = "financeAdapter")
    override fun getSettlements(groupId: String): List<Settlements> {
        return try {
            restTemplate.exchange(
                    resolveSettlementsAddress(groupId),
                    GET,
                    HttpEntity<Any>(HttpHeaders().withAppAcceptType()),
                    SettlementsResponse::class.java,
            ).body?.toDomain() ?: throw FinanceAdapterClientException("While trying to retrieve settlements we receive empty body")
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to retrieve settlements" }
            throw FinanceAdapterClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to retrieve settlements" }
            throw RetryableFinanceAdapterClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to retrieve settlements" }
            throw FinanceAdapterClientException(ex.message)
        }
    }

    private fun resolveActivitiesAddress(groupId: String) =
        "${financeAdapterProperties.url}$INTERNAL/expenses/activities/groups/$groupId"

    private fun resolveBalancesAddress(groupId: String) =
            "${financeAdapterProperties.url}$INTERNAL/balances/activities/groups/$groupId"

    private fun resolveSettlementsAddress(groupId: String) =
            "${financeAdapterProperties.url}$INTERNAL/settlements/activities/groups/$groupId"
    
    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
