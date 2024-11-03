package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.client.FinanceAdapterClient
import pl.edu.agh.gem.internal.client.RetryableFinanceAdapterClientException
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_ACTIVITIES
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_SETTLEMENTS
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.report.ReportJob

@Component
class FetchBalanceStage(
    private val financeAdapterClient: FinanceAdapterClient,
) : ProcessingStage() {
    override fun process(reportJob: ReportJob): StageResult {
        logger.info { "Fetching balances for report job: $reportJob" }
        try {
            val balances = financeAdapterClient.getBalances(reportJob.groupId)
            return nextStage(reportJob.copy(balances = balances), FETCHING_ACTIVITIES)
        } catch (e: RetryableFinanceAdapterClientException) {
            return nextStage(reportJob, FETCHING_SETTLEMENTS)
        }
    }
}
