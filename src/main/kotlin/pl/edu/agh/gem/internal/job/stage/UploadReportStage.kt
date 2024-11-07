package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.client.AttachmentStoreClient
import pl.edu.agh.gem.internal.client.RetryableAttachmentStoreClientException
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.ReportJobState.FORMAT_SELECTION
import pl.edu.agh.gem.internal.job.ReportJobState.SAVING
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.report.ReportJob

@Component
class UploadReportStage(
    private val attachmentStoreClient: AttachmentStoreClient,
) : ProcessingStage() {
    override fun process(reportJob: ReportJob): StageResult {
        try {
            return if (reportJob.file == null) {
                return nextStage(reportJob, FORMAT_SELECTION)
            } else {
                val attachment = attachmentStoreClient.uploadAttachment(
                    groupId = reportJob.groupId,
                    file = reportJob.file,
                    userId = reportJob.creatorId,
                )
                nextStage(reportJob.copy(attachmentId = attachment.id), SAVING)
            }
        } catch (e: RetryableAttachmentStoreClientException) {
            return retry()
        } catch (e: Exception) {
            return failure(e)
        }
    }
}
