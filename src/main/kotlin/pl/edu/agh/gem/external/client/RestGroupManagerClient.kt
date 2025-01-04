package pl.edu.agh.gem.external.client

import io.github.resilience4j.retry.annotation.Retry
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import pl.edu.agh.gem.config.GroupManagerProperties
import pl.edu.agh.gem.external.dto.group.GroupResponse
import pl.edu.agh.gem.external.dto.group.UserGroupsResponse
import pl.edu.agh.gem.external.dto.group.toDomain
import pl.edu.agh.gem.headers.HeadersUtils.withAppAcceptType
import pl.edu.agh.gem.headers.HeadersUtils.withAppContentType
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.client.GroupManagerClientException
import pl.edu.agh.gem.internal.client.RetryableGroupManagerClientException
import pl.edu.agh.gem.internal.model.group.Group
import pl.edu.agh.gem.internal.model.group.GroupDetails
import pl.edu.agh.gem.metrics.MeteredClient
import pl.edu.agh.gem.paths.Paths.INTERNAL
import java.io.IOException

@Component
@MeteredClient
class RestGroupManagerClient(
    @Qualifier("GroupManagerRestTemplate") val restTemplate: RestTemplate,
    val groupManagerProperties: GroupManagerProperties,
) : GroupManagerClient {

    @Retry(name = "groupManager")
    override fun getGroups(userId: String): List<Group> {
        return try {
            restTemplate.exchange(
                resolveUserGroupsAddress(userId),
                GET,
                HttpEntity<Any>(HttpHeaders().withAppAcceptType()),
                UserGroupsResponse::class.java,
            ).body?.toDomain() ?: throw GroupManagerClientException("While trying to retrieve user groups, received an empty body")
        } catch (ex: Exception) {
            handleGroupManagerException(ex, "retrieve user groups")
        }
    }

    @Retry(name = "groupManager")
    override fun getGroupDetails(groupId: String): GroupDetails {
        return try {
            restTemplate.exchange(
                resolveGroupAddress(groupId),
                GET,
                HttpEntity<Any>(HttpHeaders().withAppAcceptType().withAppContentType()),
                GroupResponse::class.java,
            ).body?.toDomain() ?: throw GroupManagerClientException("While retrieving group, received an empty body")
        } catch (ex: Exception) {
            handleGroupManagerException(ex, "get group: $groupId")
        }
    }

    private fun <T> handleGroupManagerException(ex: Exception, action: String): T {
        when (ex) {
            is HttpClientErrorException -> {
                logger.warn(ex) { "Client-side exception while trying to $action" }
                throw GroupManagerClientException(ex.message)
            }
            is HttpServerErrorException, is ResourceAccessException, is IOException -> {
                logger.warn(ex) { "Retryable exception while trying to $action" }
                throw RetryableGroupManagerClientException(ex.message)
            }
            else -> {
                logger.warn(ex) { "Unexpected exception while trying to $action" }
                throw GroupManagerClientException(ex.message)
            }
        }
    }

    private fun resolveUserGroupsAddress(userId: String) =
        "${groupManagerProperties.url}$INTERNAL/groups/users/$userId"

    private fun resolveGroupAddress(groupId: String) =
        "${groupManagerProperties.url}$INTERNAL/groups/$groupId"

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
