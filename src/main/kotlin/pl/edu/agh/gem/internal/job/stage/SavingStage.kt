package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.report.ReportJob

@Component
class SavingStage() : ProcessingStage() {
    override fun process(reportJob: ReportJob): StageResult {
        logger.info { "Saving report job: $reportJob" }
        return success()
    }
}
