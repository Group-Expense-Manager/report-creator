package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.ReportJobState.GENERATING_XLSX_REPORT
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.report.ReportFormat.XLSX
import pl.edu.agh.gem.internal.model.report.ReportJob

@Component
class FormatSelectionStage : ProcessingStage() {
    override fun process(reportJob: ReportJob): StageResult {
        logger.info { "Selecting format for report job: $reportJob" }
        when (reportJob.format) {
            XLSX -> return nextStage(reportJob, GENERATING_XLSX_REPORT)
        }
    }
}
