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
import pl.edu.agh.gem.config.UserDetailsManagerProperties
import pl.edu.agh.gem.external.dto.user.GroupUsersDetailsResponse
import pl.edu.agh.gem.external.dto.user.toDomain
import pl.edu.agh.gem.headers.HeadersUtils.withAppAcceptType
import pl.edu.agh.gem.internal.client.RetryableUserDetailsManagerClientException
import pl.edu.agh.gem.internal.client.UserDetailsManagerClient
import pl.edu.agh.gem.internal.client.UserDetailsManagerClientException
import pl.edu.agh.gem.internal.model.user.UsersDetails
import pl.edu.agh.gem.metrics.MeteredClient
import pl.edu.agh.gem.paths.Paths.INTERNAL
import java.io.IOException

@Component
@MeteredClient
class RestUserDetailsManagerClient(
    @Qualifier("UserDetailsManagerRestTemplate") val restTemplate: RestTemplate,
    val userDetailsManagerProperties: UserDetailsManagerProperties,
) : UserDetailsManagerClient {

    @Retry(name = "userDetailsManager")
    override fun getUsersDetails(groupId: String): UsersDetails {
        return try {
            restTemplate.exchange(
                resolveGroupUsersDetailsAddress(groupId),
                GET,
                HttpEntity<Any>(HttpHeaders().withAppAcceptType()),
                GroupUsersDetailsResponse::class.java,
            ).body?.toDomain() ?: throw UserDetailsManagerClientException("While trying to retrieve group users details, received an empty body")
        } catch (ex: Exception) {
            handleUserDetailsManagerException(ex, "retrieve group users details")
        }
    }

    private fun <T> handleUserDetailsManagerException(ex: Exception, action: String): T {
        when (ex) {
            is HttpClientErrorException -> {
                logger.warn(ex) { "Client-side exception while trying to $action" }
                throw UserDetailsManagerClientException(ex.message)
            }
            is HttpServerErrorException, is ResourceAccessException, is IOException -> {
                logger.warn(ex) { "Retryable exception while trying to $action" }
                throw RetryableUserDetailsManagerClientException(ex.message)
            }
            else -> {
                logger.warn(ex) { "Unexpected exception while trying to $action" }
                throw UserDetailsManagerClientException(ex.message)
            }
        }
    }

    private fun resolveGroupUsersDetailsAddress(groupId: String) =
        "${userDetailsManagerProperties.url}$INTERNAL/user-details/groups/$groupId"

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
