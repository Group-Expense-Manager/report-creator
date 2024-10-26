package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.ReportJobState
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_ACTIVITIES
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_BALANCES
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_SETTLEMENTS
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.report.ReportJob

@Component
class GenerateXSLMReportStage : ProcessingStage() {
    override fun process(reportJob: ReportJob): StageResult {
        reportJob.balances ?: return nextStage(reportJob, FETCHING_BALANCES)
        reportJob.settlements ?: return nextStage(reportJob, FETCHING_SETTLEMENTS)
        reportJob.activities ?: return nextStage(reportJob, FETCHING_ACTIVITIES)
        
        
        return success()
    }
}
