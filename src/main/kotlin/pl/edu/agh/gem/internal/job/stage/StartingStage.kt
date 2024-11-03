package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_SETTLEMENTS
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.report.ReportJob

@Component
class StartingStage : ProcessingStage() {
    override fun process(reportJob: ReportJob): StageResult {
        logger.info { "Starting report job: $reportJob" }
        return nextStage(reportJob, FETCHING_SETTLEMENTS)
    }
}
