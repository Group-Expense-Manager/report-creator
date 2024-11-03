package pl.edu.agh.gem.external.client

import io.github.resilience4j.retry.annotation.Retry
import mu.KotlinLogging
import org.bson.types.Binary
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.POST
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import pl.edu.agh.gem.config.AttachmentStoreProperties
import pl.edu.agh.gem.external.dto.attachment.AttachmentResponse
import pl.edu.agh.gem.external.dto.attachment.toDomain
import pl.edu.agh.gem.headers.HeadersUtils.withAppAcceptType
import pl.edu.agh.gem.internal.client.AttachmentStoreClient
import pl.edu.agh.gem.internal.client.AttachmentStoreClientException
import pl.edu.agh.gem.internal.client.RetryableAttachmentStoreClientException
import pl.edu.agh.gem.internal.model.finance.Attachment
import pl.edu.agh.gem.paths.Paths.INTERNAL
import java.io.IOException

@Component
class RestAttachmentStoreClient(
    @Qualifier("AttachmentStoreRestTemplate") val restTemplate: RestTemplate,
    val attachmentStoreProperties: AttachmentStoreProperties,
) : AttachmentStoreClient {

    private fun resolveUploadAttachmentAddress(groupId: String, userId: String) =
        "${attachmentStoreProperties.url}$INTERNAL/groups/$groupId?userId=$userId"

    @Retry(name = "attachmentStore")
    override fun uploadAttachment(groupId: String, userId: String, file: Binary): Attachment {
        return try {
            restTemplate.exchange(
                resolveUploadAttachmentAddress(groupId, userId),
                POST,
                HttpEntity(file, HttpHeaders().withAppAcceptType()),
                AttachmentResponse::class.java,
            ).body?.toDomain() ?: throw AttachmentStoreClientException("While trying to upload attachment, we received an empty body")
        } catch (ex: Exception) {
            handleAttachmentStoreException(ex, "upload attachment")
        }
    }

    private fun <T> handleAttachmentStoreException(ex: Exception, action: String): T {
        when (ex) {
            is HttpClientErrorException -> {
                logger.warn(ex) { "Client-side exception while trying to $action" }
                throw AttachmentStoreClientException(ex.message)
            }
            is HttpServerErrorException, is ResourceAccessException, is IOException -> {
                logger.warn(ex) { "Retryable exception while trying to $action" }
                throw RetryableAttachmentStoreClientException(ex.message)
            }
            else -> {
                logger.warn(ex) { "Unexpected exception while trying to $action" }
                throw AttachmentStoreClientException(ex.message)
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
