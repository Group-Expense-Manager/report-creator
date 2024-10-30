package pl.edu.agh.gem.integration.client

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubEmailSenderReportNotification
import pl.edu.agh.gem.internal.client.EmailSenderClient
import pl.edu.agh.gem.internal.client.EmailSenderClientException
import pl.edu.agh.gem.internal.client.RetryableEmailSenderClientException
import pl.edu.agh.gem.util.createReportNotification

class EmailSenderClientIT(
    private val emailSenderClient: EmailSenderClient,
) : BaseIntegrationSpec({
    should("send report notification") {
        // given
        val reportNotification = createReportNotification()
        stubEmailSenderReportNotification()

        // when & then
        shouldNotThrowAny {
            emailSenderClient.notifyAboutReport(
                reportId = reportNotification.id,
                title = reportNotification.title,
                userId = reportNotification.userId,
                attachmentId = reportNotification.attachmentId,
            )
        }
    }

    should("handle 4xx error response") {
        // given
        val reportNotification = createReportNotification()
        stubEmailSenderReportNotification(BAD_REQUEST)

        // when & then
        shouldThrow<EmailSenderClientException> {
            emailSenderClient.notifyAboutReport(
                reportId = reportNotification.id,
                title = reportNotification.title,
                userId = reportNotification.userId,
                attachmentId = reportNotification.attachmentId,
            )
        }
    }

    should("handle 5xx error response") {
        // given
        val reportNotification = createReportNotification()
        stubEmailSenderReportNotification(INTERNAL_SERVER_ERROR)

        // when & then
        shouldThrow<RetryableEmailSenderClientException> {
            emailSenderClient.notifyAboutReport(
                reportId = reportNotification.id,
                title = reportNotification.title,
                userId = reportNotification.userId,
                attachmentId = reportNotification.attachmentId,
            )
        }
    }
},)
