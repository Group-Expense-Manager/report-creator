package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.client.EmailSenderClient
import pl.edu.agh.gem.internal.client.RetryableEmailSenderClientException
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.ReportJobState.UPLOAD_REPORT
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.report.ReportJob

@Component
class NotifyStage(
    private val emailSenderClient: EmailSenderClient,
) : ProcessingStage() {
    override fun process(reportJob: ReportJob): StageResult {
        logger.info { "Notify about report job: $reportJob" }
        return try {
            if (reportJob.attachmentId == null) {
                nextStage(reportJob, UPLOAD_REPORT)
            } else {
                emailSenderClient.notifyAboutReport(reportJob.id, reportJob.title, reportJob.creatorId, reportJob.attachmentId)
                success()
            }
        } catch (e: RetryableEmailSenderClientException) {
            retry()
        }
    }
}
