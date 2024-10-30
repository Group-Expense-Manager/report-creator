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
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import pl.edu.agh.gem.config.EmailSenderProperties
import pl.edu.agh.gem.external.dto.report.ReportNotificationRequest
import pl.edu.agh.gem.headers.HeadersUtils.withAppContentType
import pl.edu.agh.gem.internal.client.EmailSenderClient
import pl.edu.agh.gem.internal.client.EmailSenderClientException
import pl.edu.agh.gem.internal.client.RetryableEmailSenderClientException
import pl.edu.agh.gem.paths.Paths.INTERNAL

@Component
class RestEmailSenderClient(
    @Qualifier("EmailSenderRestTemplate") val restTemplate: RestTemplate,
    val emailSenderProperties: EmailSenderProperties,
) : EmailSenderClient {

    @Retry(name = "emailSender")
    override fun notifyAboutReport(reportId: String, title: String, userId: String, attachmentId: String) {
        val body = ReportNotificationRequest(
            id = reportId,
            title = title,
            userId = userId,
            attachmentId = attachmentId,
        )
        try {
            restTemplate.exchange<Unit>(
                resolveReportNotificationAddress(),
                POST,
                HttpEntity(body, HttpHeaders().withAppContentType()),
            )
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to notify about report" }
            throw EmailSenderClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to notify about report" }
            throw RetryableEmailSenderClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to notify about report" }
            throw EmailSenderClientException(ex.message)
        }
    }

    private fun resolveReportNotificationAddress() =
        "${emailSenderProperties.url}$INTERNAL/report"

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
