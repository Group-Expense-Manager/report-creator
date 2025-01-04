package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.internal.client.EmailSenderClient
import pl.edu.agh.gem.internal.client.RetryableEmailSenderClientException
import pl.edu.agh.gem.internal.job.ReportJobState.UPLOAD_REPORT
import pl.edu.agh.gem.util.createReportJob

class NotifyStageTest : ShouldSpec({

    val emailSenderClient = mock(EmailSenderClient::class.java)
    val notifyStage = spy(NotifyStage(emailSenderClient))

    should("notify about report job and return success") {
        // given
        val attachmentId = "attachmentId"
        val reportJob = createReportJob(attachmentId = attachmentId)

        // when
        notifyStage.process(reportJob)

        // then
        verify(emailSenderClient).notifyAboutReport(reportJob.id, reportJob.title, reportJob.creatorId, attachmentId, GROUP_ID)
        verify(notifyStage).success()
    }

    should("retry on RetryableEmailSenderException") {
        // given
        val attachmentId = "attachmentId"
        val reportJob = createReportJob(attachmentId = attachmentId)
        whenever(emailSenderClient.notifyAboutReport(reportJob.id, reportJob.title, reportJob.creatorId, attachmentId, GROUP_ID))
            .thenThrow(RetryableEmailSenderClientException::class.java)

        // when
        notifyStage.process(reportJob)

        // then
        verify(emailSenderClient).notifyAboutReport(reportJob.id, reportJob.title, reportJob.creatorId, attachmentId, GROUP_ID)
        verify(notifyStage).retry()
    }

    should("proceed to next stage when attachmentId is null") {
        // given
        val reportJob = createReportJob(attachmentId = null)

        // when
        notifyStage.process(reportJob)

        // then
        verify(notifyStage).nextStage(reportJob, UPLOAD_REPORT)
    }
})
