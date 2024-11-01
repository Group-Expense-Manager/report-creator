package pl.edu.agh.gem.external.client

import io.github.resilience4j.retry.annotation.Retry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.POST
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import pl.edu.agh.gem.config.EmailSenderProperties
import pl.edu.agh.gem.external.dto.report.ReportNotificationRequest
import pl.edu.agh.gem.headers.HeadersUtils.withAppContentType
import pl.edu.agh.gem.internal.client.EmailSenderClient
import pl.edu.agh.gem.internal.client.EmailSenderClientException
import pl.edu.agh.gem.internal.client.RetryableEmailSenderClientException
import pl.edu.agh.gem.paths.Paths.INTERNAL
import java.io.IOException

@Component
class RestEmailSenderClient(
    @Qualifier("EmailSenderRestTemplate") val restTemplate: RestTemplate,
    val emailSenderProperties: EmailSenderProperties,
) : EmailSenderClient {

    @Retry(name = "emailSender")
    override fun notifyAboutReport(
        reportId: String,
        title: String,
        creatorId: String,
        attachmentId: String,
        groupId: String,
    ) {
        val body = ReportNotificationRequest(
            id = reportId,
            groupId = groupId,
            title = title,
            creatorId = creatorId,
            attachmentId = attachmentId,
        )

        try {
            restTemplate.exchange<Unit>(
                resolveReportNotificationAddress(),
                POST,
                HttpEntity(body, HttpHeaders().withAppContentType()),
            )
        } catch (ex: Exception) {
            handleEmailSenderException(ex, "notify about report")
        }
    }

    private fun <T> handleEmailSenderException(ex: Exception, action: String): T {
        when (ex) {
            is HttpClientErrorException -> {
                logger.warn(ex) { "Client-side exception while trying to $action" }
                throw EmailSenderClientException(ex.message)
            }
            is HttpServerErrorException, is ResourceAccessException, is IOException -> {
                logger.warn(ex) { "Retryable exception while trying to $action" }
                throw RetryableEmailSenderClientException(ex.message)
            }
            else -> {
                logger.warn(ex) { "Unexpected exception while trying to $action" }
                throw EmailSenderClientException(ex.message)
            }
        }
    }

    private fun resolveReportNotificationAddress() =
        "${emailSenderProperties.url}$INTERNAL/report"

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
