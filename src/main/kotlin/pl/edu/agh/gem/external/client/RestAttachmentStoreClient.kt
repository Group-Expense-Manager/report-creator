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

@Component
class RestAttachmentStoreClient(
    @Qualifier("AttachmentStoreRestTemplate") val restTemplate: RestTemplate,
    val attachmentStoreProperties: AttachmentStoreProperties,
) : AttachmentStoreClient {
    
    private fun resolveUploadAttachmentAddress(groupId: String) =
        "${attachmentStoreProperties.url}$INTERNAL/expenses/activities/groups/$groupId"
    
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Retry(name = "attachmentStore")
    override fun uploadAttachment(groupId: String,file: Binary): Attachment {
        return try {
            restTemplate.exchange(
                    resolveUploadAttachmentAddress(groupId),
                    POST,
                    HttpEntity(file,HttpHeaders().withAppAcceptType()),
                    AttachmentResponse::class.java,
            ).body?.toDomain() ?: throw AttachmentStoreClientException("While trying to upload attachment we receive empty body")
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to upload attachment" }
            throw AttachmentStoreClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to upload attachment" }
            throw RetryableAttachmentStoreClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to upload attachment" }
            throw AttachmentStoreClientException(ex.message)
        }
    }
}
