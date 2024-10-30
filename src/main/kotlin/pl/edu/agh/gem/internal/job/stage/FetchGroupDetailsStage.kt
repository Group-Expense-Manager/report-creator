package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.client.RetryableUserDetailsManagerClientException
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.ReportJobState.FORMAT_SELECTION
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.report.ReportJob

@Component
class FetchGroupDetailsStage(
    private val groupManagerClient: GroupManagerClient,
) : ProcessingStage() {
    override fun process(reportJob: ReportJob): StageResult {
        logger.info { "Fetching group details for report job: $reportJob" }
        try {
            val groupDetails = groupManagerClient.getGroupDetails(reportJob.groupId)
            return nextStage(reportJob.copy(groupDetails = groupDetails), FORMAT_SELECTION)
        } catch (e: RetryableUserDetailsManagerClientException) {
            return retry()
        }
    }
}
