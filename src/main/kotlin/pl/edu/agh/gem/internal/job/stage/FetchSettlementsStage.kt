package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.client.FinanceAdapterClient
import pl.edu.agh.gem.internal.client.RetryableFinanceAdapterClientException
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_BALANCES
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.finance.SettlementStatus.PENDING
import pl.edu.agh.gem.internal.model.report.ReportJob

@Component
class FetchSettlementsStage(
    private val financeAdapterClient: FinanceAdapterClient,
) : ProcessingStage() {
    override fun process(reportJob: ReportJob): StageResult {
        logger.info { "Fetching settlements for report job: $reportJob" }
        try {
            val settlements = financeAdapterClient.getSettlements(reportJob.groupId)
            return if (settlements.any { it.status == PENDING }) {
                retry()
            } else {
                nextStage(reportJob.copy(settlements = settlements), FETCHING_BALANCES)
            }
        } catch (e: RetryableFinanceAdapterClientException) {
            return retry()
        }
    }
}
