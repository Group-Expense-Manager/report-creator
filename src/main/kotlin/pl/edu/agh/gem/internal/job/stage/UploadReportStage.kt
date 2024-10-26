package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.client.AttachmentStoreClient
import pl.edu.agh.gem.internal.client.RetryableAttachmentStoreClientException
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.ReportJobState
import pl.edu.agh.gem.internal.job.ReportJobState.FORMAT_SELLECTION
import pl.edu.agh.gem.internal.job.ReportJobState.SAVING
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.report.ReportJob

@Component
class UploadReportStage(
        private val attachmentStoreClient: AttachmentStoreClient
) : ProcessingStage() {
    override fun process(reportJob: ReportJob): StageResult {
        try {
            if(reportJob.file == null) return nextStage(reportJob, FORMAT_SELLECTION)
            attachmentStoreClient.uploadAttachment(
                    groupId = reportJob.groupId,
                    file = reportJob.file
            )
        }
        catch (e: RetryableAttachmentStoreClientException){
            retry()
        }
        catch (e: Exception){
            return failure(e)
        }
        return nextStage(reportJob, SAVING)
    }
}
