package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.client.RetryableUserDetailsManagerClientException
import pl.edu.agh.gem.internal.client.UserDetailsManagerClient
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_GROUP_DETAILS
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.report.ReportJob

@Component
class FetchUsersDetailsStage(
    private val groupUserDetailsManagerClient: UserDetailsManagerClient,
) : ProcessingStage() {
    override fun process(reportJob: ReportJob): StageResult {
        logger.info { "Fetching users details for report job: $reportJob" }
        try {
            val usersDetails = groupUserDetailsManagerClient.getUsersDetails(reportJob.groupId)
            return nextStage(reportJob.copy(usersDetails = usersDetails), FETCHING_GROUP_DETAILS)
        } catch (e: RetryableUserDetailsManagerClientException) {
            return retry()
        }
    }
}
